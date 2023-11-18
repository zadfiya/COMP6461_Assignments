import java.io.Serializable;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = -7021695103233470277L;
    private String body;
    private String requestFileName;
    private String responseCode;
    private String responseHeaders;
    private String responseMessage;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRequestFileName() {
        return requestFileName;
    }

    public void setRequestFileName(String requestFileName) {
        this.requestFileName = requestFileName;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }


    @Override
    public String toString() {
        return "ServerResponse [responseHeaders=" + responseHeaders + ", body=" + body + ", responseCode="
                + responseCode + ", responseMessage=" + responseMessage + ", requestFileName=" + requestFileName + "]";
    }
}
