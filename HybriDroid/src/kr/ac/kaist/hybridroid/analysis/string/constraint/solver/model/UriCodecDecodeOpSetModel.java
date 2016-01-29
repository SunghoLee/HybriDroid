package kr.ac.kaist.hybridroid.analysis.string.constraint.solver.model;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.IDomain;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IBooleanValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IStringValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.IValue;
import kr.ac.kaist.hybridroid.analysis.string.constraint.solver.domain.value.StringTopValue;

public class UriCodecDecodeOpSetModel implements IOperationModel<IValue> {

	@SuppressWarnings({ "unused", "unchecked" })
	@Override
	public IValue apply(IValue... args) {
		// TODO Auto-generated method stub
		if(args.length != 4)
			throw new InternalError("UriCodecDecode method must have four in-edges.");
		
		IStringValue absStr = (IStringValue)args[0];
		IBooleanValue absCv = (IBooleanValue)args[1];
		IStringValue absCs = (IStringValue)args[2];
		IBooleanValue absTof = (IBooleanValue)args[3];
		
		if(absStr instanceof StringTopValue || absCs instanceof StringTopValue)
			return StringTopValue.getInstance();
		else{
			// this part is domain specific!
			IDomain strDomain = absStr.getDomain();
			IDomain boolDomain = absCv.getDomain();
			
			Set<String> str = (Set<String>)strDomain.getOperator().gamma(absStr);
			Set<String> cs = (Set<String>)strDomain.getOperator().gamma(absCs);
			
			Set<Boolean> cv = (Set<Boolean>)strDomain.getOperator().gamma(absCv);
			Set<Boolean> tof = (Set<Boolean>)strDomain.getOperator().gamma(absTof);
			
			Set<String> res = new HashSet<String>();
			for(String s : str){
				for(String css : cs){
					for(boolean cvv : cv){
						for(boolean toff : tof){
							res.add(decode(s, cvv, Charset.forName(css), toff));
						}
					}
				}
			}
			return strDomain.getOperator().alpha(res);
		}
	}

	private String decode(String s, boolean convertPlus, Charset charset,
            boolean throwOnFailure) {
        if (s.indexOf('%') == -1 && (!convertPlus || s.indexOf('+') == -1)) {
            return s;
        }
        StringBuilder result = new StringBuilder(s.length());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < s.length();) {
            char c = s.charAt(i);
            if (c == '%') {
                do {
                    int d1, d2;
                    if (i + 2 < s.length()
                            && (d1 = hexToInt(s.charAt(i + 1))) != -1
                            && (d2 = hexToInt(s.charAt(i + 2))) != -1) {
                        out.write((byte) ((d1 << 4) + d2));
                    } else if (throwOnFailure) {
                        throw new IllegalArgumentException("Invalid % sequence at " + i + ": " + s);
                    } else {
                        byte[] replacement = "\ufffd".getBytes(charset);
                        out.write(replacement, 0, replacement.length);
                    }
                    i += 3;
                } while (i < s.length() && s.charAt(i) == '%');
                result.append(new String(out.toByteArray(), charset));
                out.reset();
            } else {
                if (convertPlus && c == '+') {
                    c = ' ';
                }
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }
	
	private int hexToInt(char c) {
        if ('0' <= c && c <= '9') {
            return c - '0';
        } else if ('a' <= c && c <= 'f') {
            return 10 + (c - 'a');
        } else if ('A' <= c && c <= 'F') {
            return 10 + (c - 'A');
        } else {
            return -1;
        }
    }
}
