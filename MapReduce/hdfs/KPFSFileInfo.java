/**
 * 
 */
package hdfs;

import java.io.Serializable;

/**
 * KPFSFileInfo
 * 
 * Used in data master to store the location and size of a KPFile.
 *
 */
public class KPFSFileInfo implements Serializable {

	private static final long serialVersionUID = -8638758877808784241L;
	public long _size = 0;
	public int _sid = -1;
	
	public KPFSFileInfo() {
		
	}
	
	public KPFSFileInfo(int sid, long size) {
		_sid = sid;
		_size = size;
	}
	
	/* 
	 * For being used in Set 
	 */
	@Override
    public int hashCode() {
		return _sid;
	}
	
	/* 
	 * For being used in Set 
	 */
	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final KPFSFileInfo other = (KPFSFileInfo) obj;
        return _sid==other._sid;
        
    }
}
