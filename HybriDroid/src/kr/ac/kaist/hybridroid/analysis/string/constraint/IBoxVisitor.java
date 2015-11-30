package kr.ac.kaist.hybridroid.analysis.string.constraint;

public interface IBoxVisitor<T> {
	public T visit(VarBox b);
	public T visit(ParamBox b);
	public T visit(ConstBox b);
}
