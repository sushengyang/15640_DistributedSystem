package edu.cmu.andrew.ds.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;


/**
 * TransactionalFileInputStream 
 * 
 * A transactional input file class to facilitate migrating processes with open files.
 * 
 * When a read is requested, the class opens a file, seeks to the requisite location, performs the operation, 
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

public class TransactionalFileInputStream extends InputStream implements Serializable {
	private static final long serialVersionUID = 8418840253669323271L;

	private static final String TAG = TransactionalFileInputStream.class.getSimpleName();
	
	/*
	 * In order to improve performance, you can also choose to “cache” these connections by reusing them, 
	 * unless a “migrated” flag is set, etc, in which case you would set the flag upon migration and reset
	 * it any time a file handle is created or renewed.
	 */
	
	private File _src;
	private long _pos;
	private transient RandomAccessFile _hdl;
	private boolean _migrating;
	private boolean _isReading = false;
	
	public TransactionalFileInputStream(String path) 
			throws IOException {
		this._src = new File(path);
        this._pos = 0;
        this._migrating = false;
	}
	
	/* read in one byte */
	@Override
    public synchronized int read() throws IOException {
		/* if is migrating, wait */
		while (_migrating == true) {
			println("waiting for completion of migration");
			try {
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		
        /* set the reading lock to make sure it won't enter migrating mode */
		setReading();
		
		if (_hdl == null) {
            _hdl = new RandomAccessFile(_src, "r");
            _hdl.seek(_pos);
        }
        int result = _hdl.read();
        _pos++;
				
        /* notify other waiting threads before migrating */
		notify();
		
		/* reset the reading lock to make it ready to enter migrating mode */
		resetReading();
        
        return result;
    }
	
	/* suspend before migrate */
	public synchronized void suspend() 
			throws IOException {
		/* ensure one instance is suspended only once */
		if (_migrating == true) {
			println("WARNING: try to suspend a suspended in stream!");
			return;
		}
		
		/* ensure no reading operation is working */
		while (_isReading == true) {
			println("waiting for reading lock");
			try{
				wait();
			} catch(InterruptedException e) { } 
			finally { }
		}
		_migrating = true;
		
		close();
		_hdl = null;
		
		println("in stream suspended");
	}
	
	/* resume after migrate */
	public synchronized void resume() 
			throws IOException {
		/* resuming a non-migrating stream is meaningless */
		if (_migrating == false) {
			return;
		}
		
		_hdl = new RandomAccessFile(_src, "r");
        _hdl.seek(_pos);
		
		/* mark the end of the migration and notify other waiting threads */
		_migrating = false;
		notify();
		
		println("in stream resumed");
	}
	
	private void println(String msg) {
		System.out.println(TAG + ": " + msg);
	}
	
	@Override
    public void close() throws IOException {
		if(_hdl != null) {
    	    _hdl.close();
		}
    }
	
	private void setReading() {
		_isReading = true;
	}

	private synchronized void resetReading() {
		_isReading = false;
		notify();
	}
}
