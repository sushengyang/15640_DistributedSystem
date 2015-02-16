package edu.cmu.andrew.ds.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * TransactionalFileOutputStream 
 * 
 * A transactional output file class to facilitate migrating processes with open files.
 * 
 * When a write is requested, the class opens a file, seeks to the requisite location, performs the operation,
 * and closes the file again. In this way, it will maintain all the information required in order to continue
 * performing operations on the file, even if the process is transferred to another node.
 * 
 * It is assumed that all of the nodes share a common distributed file system, such as AFS, where all 
 * of the files to be accessed will be located. And mutexes are used to avoid interrupting these methods with migration.
 * 
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 *
 */

public class TransactionalFileOutputStream  extends OutputStream implements Serializable {
	
	/*
	 * In order to improve performance, you can also choose to “cache” these connections by reusing them, 
	 * unless a “migrated” flag is set, etc, in which case you would set the flag upon migration and reset
	 * it any time a file handle is created or renewed.
	 */
	
	private static final long serialVersionUID = -2615966902694975193L;
	private File _dst = null;
	private long _pos = 0;
	private transient RandomAccessFile _hdl = null;
	private boolean _migrating = false;	
	private boolean _isWriting = false;
	
	public TransactionalFileOutputStream(String path) throws IOException {
		this._dst = new File(path);
		/* to allow all other users can write */
		Runtime.getRuntime().exec("chmod 777 " + path);
        this._pos = 0;
        this._migrating = false;
	}
	
	@Override
    public synchronized void write(int b) throws IOException {
		/* if is migrating, wait */
		while (_migrating == true) {
			println("waiting for completion of migration");
			try {
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		
		/* set the reading lock to make sure it won't enter migrating mode */
		setWriting();
		
        if (_hdl == null) {
        	_hdl = new RandomAccessFile(_dst, "rw");
        	_hdl.seek(_pos);
        }
        _hdl.write(b);
        _pos++;
        
        /* notifyAll other waiting threads before migrating */
		notifyAll();
		
		/* reset the reading lock to make it ready to enter migrating mode */
		resetWriting();
    }
	
	/* suspend before migrate */
	public synchronized void suspend() 
			throws IOException {
		/* ensure one instance is suspended only once */
		if (_migrating == true) {
			return;
		}
		_migrating = true;
		
		/* ensure no writing operation is working */
		while (_isWriting == true) {
			println("waiting for writing lock");
			try{
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		
		close();
		_hdl = null;
		
		println("out stream suspended");
	}
	
	/* resume after migrate */
	public synchronized void resume() 
			throws IOException {
		/* resuming a non-migrating stream is meaningless */
		if (_migrating == false) {
			println("WARNING: try to resume a non-migrating out stream!");
			return;
		}
		
		_hdl = new RandomAccessFile(_dst, "rw");
    	_hdl.seek(_pos);
		
		/* mark the end of the migration and notifyAll other waiting threads */
		_migrating = false;
		notifyAll();
		
		println("out stream resumed");
	}
	
	/**
     * close the handle
     */
    @Override
    public void close() throws IOException {
    	if(_hdl != null) {
    		_hdl.close();
    	}
    }
    
    public void setWriting() {
		_isWriting = true;
	}
	public synchronized void resetWriting() {
		_isWriting = false;
		notifyAll();
	}
	
	private void println(String msg) {
		System.out.println("TransactionalFileOutputStream: " + msg);
	}
}