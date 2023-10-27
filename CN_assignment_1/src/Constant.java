public class Constant {
    public static String HTTPC = "httpc";
    public static String INVALID_COMMAND = "invalid Command!!";
    public static String REDIRECTION_SUCCESSFUL = "Redirection Successful :)";
    public static String REDIRECTION_REQUEST = "httpc get -v http://httpbin.org/get";
    public static String InlineFileDataError = "Either [-d] or [-f] can be used but not both.";
    public static String NEW__LINE = "\n";
    public static String GET = "get";
    public static String POST ="post";
    public static String HELP = "help";

    public static void setRedirectionRequest(String req)
    {
        REDIRECTION_REQUEST = "httpc get -v http://httpbin.org/get" + req;
    }
    public static String helpMenuPrint(String type)
    {
        switch(type)
        {
            case "get":
            {
                return ("usage: httpc get [-v] [-h key:value] URL\nGet executes a HTTP GET request for "
                        + "a given URL.\n\n  -v Prints the detail of the response such as protocol, status, and headers."
                        + "\n  -h key:value Associates headers to HTTP Request with the format 'key:value'.");

            }
            case "post":
            {
                return ("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\nPost executes a HTTP "
                        + "POST request for a given URL with inline data or from file.\n  -v Prints the detail of the response "
                        + "such as protocol, status, and headers.\n  -h key:value Associates headers to HTTP Request with the "
                        + "format 'key:value'.\n  -d string Associates an inline data to the body HTTP POST request."
                        + "\n  -f file Associates the content of a file to the body HTTP POST request.\n\nEither [-d] or [-f] "
                        + "can be used but not both.");

            }


        };
        return ("httpc is a curl-like application but supports HTTP protocol only.\n"
                + "Usage:\n  httpc command [arguments]\nThe commands are:\n  get  executes a HTTP GET request and prints the response.\n"
                + "  post executes a HTTP POST request and prints the response.\n  help prints this screen.\n\nUse \"httpc help [command]\" "
                + "for more information about a command.");
    }

    public static String NEW_LINE = "\n"
            +"Access-Control-Allow-Origin: *\n" +
            "Access-Control-Allow-Credentials: true\n" +
            "\n" +
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n" +
            "<title>Redirecting...</title>\n" +
            "<h1>Redirecting...</h1>\n" +
            "<p>You should be redirected automatically to target URL: <a href=\"/get\">/get</a>.  If not click the link.\n";

}
