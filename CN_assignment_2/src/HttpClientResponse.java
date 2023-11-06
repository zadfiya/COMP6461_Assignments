import java.io.Serializable;

/**
 * POJO class for Server Response
 * 
 * @author <a href="mailto:z_tel@encs.concordia.ca">Zankhanaben Patel</a>
 */
public class HttpClientResponse implements Serializable {

	private static final long serialVersionUID = -2167025103933277470L;
	private String responseHeaders;
	private String body;
	private String responseCode;
	private String responseMessage;
	private String requestFileName;

	public String getResponseHeaders() {
		return responseHeaders;
	}

	public void setResponseHeaders(String responseHeaders) {
		this.responseHeaders = responseHeaders;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getRequestFileName() {
		return requestFileName;
	}

	public void setRequestFileName(String requestFileName) {
		this.requestFileName = requestFileName;
	}

	@Override
	public String toString() {
		return "HttpClientResponse [responseHeaders=" + responseHeaders + ", body=" + body + ", responseCode="
				+ responseCode + ", responseMessage=" + responseMessage + ", requestFileName=" + requestFileName + "]";
	}

}
