package org.servicemix.ws.rm;


public class SoapFault extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String code;
    private String subCode;
    private String detail;
    
    public SoapFault(String reason, String code, String subCode, String detail) {
        super(reason);
        this.code = code;
        this.subCode = subCode;
        this.detail = detail;
    }
    
    public String getCode() {
        return code;
    }
    public String getDetail() {
        return detail;
    }
    public String getSubCode() {
        return subCode;
    }
    
}
