import api.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;

public class Runner {
    public static void main(String[] args) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        Mappings mappings = new Mappings();
        mappings.addMap("GET","/", "html/index.html");
        mappings.addMap("GET","/SAVE", "html/save.html");
        mappings.addMap("POST","/SAVE", "html/save.html");
        mappings.addMap("GET", "/dome", new AbstractResponse() {
            @Override
            public Response getResponse(Request req) {
                String res = "<html><body>";
                res+="Msg received:" + req.getAttribute("msg")+"<br>";
                res+="<a href='/'>Home</a>";
                res+="</body></html>";
                Response resp = new Response(res);
                return resp;
            }
        });


        String ksName = "sslServer.jks";
        char ksPass[] = "changeit".toCharArray();
        char ctPass[] = "changeit".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(ksName), ksPass);
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, ctPass);
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory ssf = sc.getServerSocketFactory();


        while (true) {
            SSLServerSocket server = (SSLServerSocket) ssf.createServerSocket(8888);
            Connection client = new Connection((SSLSocket) server.accept(), mappings);
            new Thread(()->{
                try {
                    Request req = client.readRequest();
                    client.sendResponse(req);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            server.close();
        }
    }
}
