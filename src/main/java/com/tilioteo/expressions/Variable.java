/**
 * 
 */
package com.tilioteo.expressions;

import com.tilioteo.common.Strings;

/**
 * @author Kamil Morong - Hypothesis
 *
 */
@SuppressWarnings("serial")
public class Variable extends Primitive {

	private int refCount;
	private String name;
	private Class<?> type;

	protected int decRefCount() {
		return (refCount > 1 ? --refCount : 0);
	}

	protected void incRefCount() {
		++refCount;
	}

	public Variable(String name, Class<?> type) {
		assert (!Strings.isNullOrEmpty(name));

		this.refCount = 0;
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	@Override
	public void setValue(Object value) {
		boolean assigned = false;
		if (value != null) {
			Class<?> valueType = value.getClass();

			if (String.class == valueType) {
				String strValue = (String) value;

				if (Byte.class == type) {
					try {
						Byte val = Byte.parseByte(strValue);
						super.setValue(val);
						assigned = true;
					} catch (NumberFormatException e) {
					}

				} else if (Short.class == type) {
					try {
						Short val = Short.parseShort(strValue);
						super.setValue(val);
						assigned = true;
					} catch (NumberFormatException e) {
					}

				} else if (Integer.class == type) {
					try {
						Integer val = Integer.parseInt(strValue);
						super.setValue(val);
						assigned = true;
					} catch (NumberFormatException e) {
					}

				} else if (Long.class == type) {
					try {
						Long val = Long.parseLong(strValue);
						super.setValue(val);
						assigned = true;
					} catch (NumberFormatException e) {
					}

				} else if (Float.class == type) {
					try {
						Float val = Float.parseFloat(strValue);
						super.setValue(val);
						assigned = true;
					} catch (NumberFormatException e) {
					}

				} else if (Double.class == type) {
					try {
						Double val = Double.parseDouble(strValue);
						super.setValue(val);
						assigned = true;
					} catch (NumberFormatException e) {
					}

				} else if (Boolean.class == type) {
					if (strValue.equalsIgnoreCase(StringConstants.STR_BOOL_TRUE)) {
						super.setValue(Boolean.TRUE);
						assigned = true;
					} else if (strValue.equalsIgnoreCase(StringConstants.STR_BOOL_FALSE)) {
						super.setValue(Boolean.FALSE);
						assigned = true;
					}
				} else if (type.isAssignableFrom(valueType)) {
					super.setValue(value);
					trySetPrimitiveType(value);
					assigned = true;
				}
			}

			if (!assigned && Number.class.isAssignableFrom(valueType)) {
				Number val = (Number) value;
				if (Byte.class == type) {
					super.setValue(val.byteValue());
					assigned = true;
				} else if (Short.class == type) {
					super.setValue(val.shortValue());
					assigned = true;
				} else if (Integer.class == type) {
					super.setValue(val.intValue());
					assigned = true;
				} else if (Long.class == type) {
					super.setValue(val.longValue());
					assigned = true;
				} else if (Float.class == type) {
					super.setValue(val.floatValue());
					assigned = true;
				} else if (Double.class == type) {
					super.setValue(val.doubleValue());
					assigned = true;
				} else if (Object.class == type) {
					super.setValue(value);
					trySetPrimitiveType(value);
					assigned = true;
				}

			}

			if (!assigned && Boolean.class == valueType) {
				Boolean val = (Boolean) value;

				if (Boolean.class == type) {
					super.setValue(val);
					assigned = true;
				} else if (Byte.class == type) {
					super.setValue(new Byte(val ? (byte) 1 : 0));
					assigned = true;
				} else if (Short.class == type) {
					super.setValue(new Short(val ? (short) 1 : 0));
					assigned = true;
				} else if (Integer.class == type) {
					super.setValue(new Integer(val ? 1 : 0));
					assigned = true;
				} else if (Long.class == type) {
					super.setValue(new Long(val ? 1L : 0));
					assigned = true;
				} else if (Float.class == type) {
					super.setValue(new Float(val ? 1f : 0));
					assigned = true;
				} else if (Double.class == type) {
					super.setValue(new Double(val ? 1.0 : 0));
					assigned = true;
				}

			}

			if (!assigned && Object.class == type) {
				super.setValue(value);
				trySetPrimitiveType(value);
				assigned = true;
			}
		}

		if (!assigned) {
			super.setValue(null);
		}
	}

	private void trySetPrimitiveType(Object value) {
		if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
				|| value instanceof Float || value instanceof Double || value instanceof Boolean
				|| value instanceof String) {
			type = value.getClass();

		} else {
			type = Object.class;
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
