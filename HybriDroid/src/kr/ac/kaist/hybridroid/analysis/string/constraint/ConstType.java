package kr.ac.kaist.hybridroid.analysis.string.constraint;

public enum ConstType {
	STRING,
	INT,
	BOOL,
	DOUBLE;
	
	private String name;
	
	static{
		STRING.name = "String";
		INT.name = "int";
		BOOL.name = "bool";
		DOUBLE.name = "double";
	}
	
	public String toString(){
		return name;
	}
}
