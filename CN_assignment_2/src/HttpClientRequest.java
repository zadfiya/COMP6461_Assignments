import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO class for Client Request
 * 
 * @author <a href="mailto:z_tel@encs.concordia.ca">Zankhanaben Patel</a>
 */
public class HttpClientRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2207162898379302282L;
	private String requestUrl;
	private String inlineData;
	private String redirectLocation;
	private String requestMethod;
	private String httpRequest;
	private String fileSendPath;
	private String fileSendData;
	private String fileWritePath;
	private boolean isVerbosePreset;
	private boolean isHttpHeader;
	private boolean isInlineData;
	private boolean isFilesend;
	private boolean isFileWrite;
	private boolean isRedirect;
	private String clientType;
	private String message;
	
	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	List<String> headerLst = new ArrayList<String>();

	public String getRequestUrl() {
		return requestUrl;
	}

	public String getFileSendData() {
		return fileSendData;
	}

	public void setFileSendData(String fileSendData) {
		this.fileSendData = fileSendData;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getInlineData() {
		return inlineData;
	}

	public void setInlineData(String inlineData) {
		this.inlineData = inlineData;
	}

	public String getRedirectLocation() {
		return redirectLocation;
	}

	public void setRedirectLocation(String redirectLocation) {
		this.redirectLocation = redirectLocation;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public String getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(String httpRequest) {
		this.httpRequest = httpRequest;
	}

	public boolean isVerbosePreset() {
		return isVerbosePreset;
	}

	public void setVerbosePreset(boolean isVerbosePreset) {
		this.isVerbosePreset = isVerbosePreset;
	}

	public boolean isHttpHeader() {
		return isHttpHeader;
	}

	public void setHttpHeader(boolean isHttpHeader) {
		this.isHttpHeader = isHttpHeader;
	}

	public boolean isInlineData() {
		return isInlineData;
	}

	public void setInlineData(boolean isInlineData) {
		this.isInlineData = isInlineData;
	}

	public boolean isFilesend() {
		return isFilesend;
	}

	public void setFilesend(boolean isFilesend) {
		this.isFilesend = isFilesend;
	}

	public boolean isFileWrite() {
		return isFileWrite;
	}

	public void setFileWrite(boolean isFileWrite) {
		this.isFileWrite = isFileWrite;
	}

	public String getFileSendPath() {
		return fileSendPath;
	}

	public void setFileSendPath(String fileSendPath) {
		this.fileSendPath = fileSendPath;
	}

	public String getFileWritePath() {
		return fileWritePath;
	}

	public void setFileWritePath(String fileWritePath) {
		this.fileWritePath = fileWritePath;
	}

	public boolean isRedirect() {
		return isRedirect;
	}

	public void setRedirect(boolean isRedirect) {
		this.isRedirect = isRedirect;
	}

	public List<String> getHeaderLst() {
		return headerLst;
	}

	public void setHeaderLst(List<String> headerLst) {
		this.headerLst = headerLst;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "HttpClientRequest [requestUrl=" + requestUrl + ", inlineData=" + inlineData + ", redirectLocation="
				+ redirectLocation + ", requestMethod=" + requestMethod + ", httpRequest=" + httpRequest
				+ ", fileSendPath=" + fileSendPath + ", fileSendData=" + fileSendData + ", fileWritePath="
				+ fileWritePath + ", isVerbosePreset=" + isVerbosePreset + ", isHttpHeader=" + isHttpHeader
				+ ", isInlineData=" + isInlineData + ", isFilesend=" + isFilesend + ", isFileWrite=" + isFileWrite
				+ ", isRedirect=" + isRedirect + ", clientType=" + clientType + ", message=" + message + ", headerLst="
				+ headerLst + "]";
	}
	
}
