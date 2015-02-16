package edu.cmu.andrew.ds.ps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import edu.cmu.andrew.ds.io.TransactionalFileInputStream;
import edu.cmu.andrew.ds.io.TransactionalFileOutputStream;

/**
 * IOProcess
 * 
 * Read input file(shuffled alphabet) by one byte a time, put the character into an array and sort,
 * and then output to a file, in order to test the kind of process using the TransactionIO library.
 *
 * @author KAIILANG CHEN(kailianc)
 * @author YANG PAN(yangpan)
 * @version 1.0
 * 
 */
public class IOProcess extends MigratableProcess {	
	private static final long serialVersionUID = -5736138960128297174L;

	private static final String TAG = IOProcess.class.getSimpleName();
	
	private int _readCharNum;
	private int _writeCharNum;
	
	private ArrayList<Integer> _buffer = new ArrayList<Integer>();
	
	private enum PROCESS { READ, SORT, WRITE, FINISH };	
	private PROCESS _proc  = PROCESS.READ;
	
	/*
	 * It is safe to assume that the process will limit it’s I/O to files accessed 
     * via the TransactionalFileInputStream and TransactionalFileOutputStream classes
	 */
	private TransactionalFileInputStream _inputStream = null;
	private TransactionalFileOutputStream _outputStream = null;
	
	private volatile boolean _suspending;
	private int id;
	
	private static final int MAX_LOOP_NUM = 8;
	private int loopNum = 0;
	/*
	 *  Every class implements MigratableProcess should have a such Constructor.
	 *  
	 *  Doing this cleans up the interface, and is more likely to lead to a 
	 *  general-purpose framework than more complex options.
	 */
	public IOProcess(String[] str) {
		this._suspending = false;
		try {
			File outFile = new File(str[1]);
			outFile.delete();
			
			this._inputStream = new TransactionalFileInputStream(str[0]);
			this._outputStream = new TransactionalFileOutputStream(str[1]);	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		System.out.println(TAG + " : run() begin, readCharNum = " + _readCharNum + 
				", writeCharNum = " + _writeCharNum + ", proc = " + _proc);
		Integer num = 0;
		while(!_suspending) {
        	switch(_proc) {
        	case READ:
        		try {
					while((num = _inputStream.read()) != -1) {
						_readCharNum++;
						_buffer.add(num);
					}
					_proc = PROCESS.SORT;
					System.out.println("READ -> SORT");
					Thread.sleep(2000);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
        		break;
        	case SORT:
        		try {
        			Collections.sort(_buffer);
            		_proc = PROCESS.WRITE;
            		System.out.println("SORT -> WRITE");
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}        		
        		break;
        	case WRITE:
        		try {
					for(Integer i : _buffer) {
						_outputStream.write(i);
						_writeCharNum++;
						Thread.sleep(200);
					}
					_proc = PROCESS.FINISH;
					System.out.println("WRITE -> FINISH");
					Thread.sleep(500);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				} 
        		break;
        	case FINISH:
        		loopNum++;
				if(loopNum == MAX_LOOP_NUM) {
					_suspending = true;
					System.out.println("JOB COMPLETED");
					return;
				}				
				_proc = PROCESS.READ;
				System.out.println("FINISH -> READ");
				break;
        	default:
        		break;
        	}
		}
		_suspending = false;
	}

	@Override
	public void suspend() {
		System.out.println(TAG + " : suspend(), readCharNum = " + _readCharNum + 
				", writeCharNum = " + _writeCharNum + ", proc = " + _proc);

		_suspending = true;		
		try {
			_inputStream.suspend();
			_outputStream.suspend();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void resume() {
		System.out.println(TAG + " : resume()");
		
		try {
			_inputStream.resume();
			_outputStream.resume();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		_suspending = false;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getSimpleName());
        sb.append("(" + id + "): ");        
        return sb.toString();
	}
}
