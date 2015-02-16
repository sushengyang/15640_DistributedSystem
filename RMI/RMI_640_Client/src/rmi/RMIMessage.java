/**
 * 
 */
package rmi;

import java.io.Serializable;
import java.util.Arrays;

/**
 * RMIMessage
 * 
 * A structure for communicating between server and client. Two fields indicate 
 * the message type(_type) and message body(_content). Every network message must use this structure.
 * 
 * Message types and description: 
 * 		type			content								description
 * 		LOOKUP			a string of the object name			client->server, to get a ROR for a name
 * 		LOOKUP_RESPOND	RMIObjectReference or null			server->client, respond to LOOKUP
 * 		CALL			{ROR, method name, args...}			client->server, invoke a remote method of a ROR
 * 		CALL_RESPOND	Object or RMIObjectReference		server->client, return value or 
 * 															RMIObjectReference(if return value is subclass of RMIService)
 * 		EXCEPTION		RMIException						server->client, used when exception thrown
 * 				
 * @author Yang Pan (yangpan)
 * @author KaiLiang Chen (kailianc)
 */
public class RMIMessage implements Serializable {

	private static final long serialVersionUID = 8896593163858381713L;
	public enum RMIMsgType{
		LOOKUP,
		LOOKUP_RESPOND,
		CALL,
		CALL_RESPOND,
		LIST,
		LIST_RESPOND,
		EXCEPTION
	};
	
	public RMIMsgType _type;
	public Object _content;
	
	/*  ======== interfaces used when _type is CALL ========
	 *  content is of type Object[] with the structure
	 *  {ROR, method name, args...}	
	 */
	public RMIObjectReference getROR() {
		if (_type != RMIMsgType.CALL) {
			return null;
		}
		return (RMIObjectReference)(((Object[])_content)[0]);
	}
	public String getMethodName() {
		if (_type != RMIMsgType.CALL) {
			return null;
		}
		return (String)(((Object[])_content)[1]);
	}
	public Object[] getArguments() {
		if (_type != RMIMsgType.CALL) {
			return null;
		}
		Object[] args = Arrays.copyOfRange((Object[])_content, 2, ((Object[])_content).length);
		
		Object[] originArgs = new Object[args.length];
		
		for (int i=0; i<args.length; ++i) {
			originArgs[i] = toOriginalArg(args[i]);
		}
		return originArgs;
	}
	
	/*
	 * In the network communication, all primitive types will cast to wrapper class.
	 * But if the argument type in the prototype of a method is primitive, you have to
	 * cast the wrapper class to primitive type so as to find the method with reflection. 
	 */
	public Class<?>[] getArgType() {
		if (_type != RMIMsgType.CALL) {
			return null;
		}
		Object[] nobj = Arrays.copyOfRange((Object[])_content, 2, ((Object[])_content).length);
		Class<?>[] types = new Class<?>[nobj.length];
		for (int i=0; i<nobj.length; ++i) {
			if (nobj[i].getClass() == Boolean[].class) {
				types[i] = boolean[].class;
			} else if (nobj[i].getClass() == Character[].class) {
				types[i] = char[].class;
			} else if (nobj[i].getClass() == Byte[].class) {
				types[i] = byte[].class;
			} else if (nobj[i].getClass() == Short[].class) {
				types[i] = short[].class;
			} else if (nobj[i].getClass() == Integer[].class) {
				types[i] = int[].class;
			} else if (nobj[i].getClass() == Long[].class) {
				types[i] = long[].class;
			} else if (nobj[i].getClass() == Float[].class) {
				types[i] = float[].class;
			} else if (nobj[i].getClass() == Double[].class) {
				types[i] = double[].class;
			} else if (nobj[i].getClass() == Character.class) {
				types[i] = char.class;
			} else if (nobj[i].getClass() == Byte.class) {
				types[i] = byte.class;
			} else if (nobj[i].getClass() == Boolean.class) {
				types[i] = boolean.class;
			} else if (nobj[i].getClass() == Short.class) {
				types[i] = short.class;
			} else if (nobj[i].getClass() == Integer.class) {
				types[i] = int.class;
			} else if (nobj[i].getClass() == Long.class) {
				types[i] = long.class;
			} else if (nobj[i].getClass() == Float.class) {
				types[i] = float.class;
			} else if (nobj[i].getClass() == Double.class) {
				types[i] = double.class;
			} else {
				types[i] = nobj[i].getClass();
			}
		}
		return types;
	}
	/* ======== CALL interfaces end ======== */
	
	
	/*
	 * internal method to cast from wrapper class to primitive
	 */
	Object toOriginalArg(Object obj) {
		if (obj.getClass() == Integer[].class) {
			Integer[] nobj = (Integer[])obj;
			int ret[] = new int[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (int) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Boolean[].class) {
			Boolean[] nobj = (Boolean[])obj;
			boolean ret[] = new boolean[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (boolean) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Byte[].class) {
			Byte[] nobj = (Byte[])obj;
			byte ret[] = new byte[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (byte) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Short[].class) {
			Short[] nobj = (Short[])obj;
			short ret[] = new short[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (short) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Character[].class) {
			Character[] nobj = (Character[])obj;
			char ret[] = new char[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (char) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Long[].class) {
			Long[] nobj = (Long[])obj;
			long ret[] = new long[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (long) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Float[].class) {
			Float[] nobj = (Float[])obj;
			float ret[] = new float[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (float) toOriginalArg(nobj[i]);
			}
			return ret;
		} else if (obj.getClass() == Double[].class) {
			Double[] nobj = (Double[])obj;
			double ret[] = new double[nobj.length];
			for (int i=0; i<nobj.length; ++i) {
				ret[i] = (double) toOriginalArg(nobj[i]);
			}
		} 
		return obj;
	}
	
}
