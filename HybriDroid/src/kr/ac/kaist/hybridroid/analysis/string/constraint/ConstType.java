package kr.ac.kaist.hybridroid.analysis.string.constraint;

public enum ConstType {
	STRING,
	INT,
	BOOL,
	DOUBLE,
	STRING_TOP,
	INT_TOP,
	BOOL_TOP,
	DOUBLE_TOP,
	UNKNOWN;
	
	private String name;
	
	static{
		STRING.name = "String";
		INT.name = "int";
		BOOL.name = "bool";
		DOUBLE.name = "double";
		STRING_TOP.name = "StringTop";
		INT_TOP.name = "IntTop";
		BOOL_TOP.name = "BoolTop";
		DOUBLE_TOP.name = "DoubleTop";
		UNKNOWN.name = "unknown";
	}
	
	public String toString(){
		return name;
	}
}
