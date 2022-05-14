package api;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Connection{

    private int PORT;
    private SSLSocket client;
    private Mappings mappings;

    public Connection(SSLSocket client, Mappings mappings) throws IOException {
        this.client = client;
        this.mappings = mappings;
    }

    public Request readRequest() throws IOException {
        InputStream is=client.getInputStream();
        int c;
        String raw = "";
        do {
            c = is.read();
            raw+=(char)c;
        } while(is.available()>0);
        Request request = new Request(raw);
        return request;
    }


    private Response getResponse(Request req) {
        AbstractResponse respAbs = mappings.getMap(req.getMethod()+"_"+req.getUrl());
        if(respAbs == null) {
            return new Response("<html><body><font color='red' size='2'>Invalid URL/method</font><br>URL: "+ req.getUrl() +"<br>method: "+ req.getMethod() +"</body></html>");
        }
        Response resp = respAbs.getResponse(req);
        return resp;
    }

    public void sendResponse(Request req) throws IOException {
        Response resp = getResponse(req);
        OutputStream out = client.getOutputStream();
        out.write(resp.toString().getBytes());
    }

}
