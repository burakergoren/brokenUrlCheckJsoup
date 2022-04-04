import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BrokenLinkCheck {

  private static final String baseUrl = "https://www.greenpeace.org/turkey/";
  private static final HashMap<String, Integer> brokenLinks = new HashMap<>();
  private static final HashMap<String, String> catchedLinks = new HashMap<>();

  public static void main(String[] args) throws IOException {
    Connection connection = Jsoup.connect(baseUrl);
    Document doc = connection.get();
    doc.select("a")
        .stream()
        .parallel()
        .map(element -> element.getElementsByAttribute("href")) //map href elements
        .map(elements -> elements.attr("href"))
        .filter(link -> !link.isEmpty()) //filter the non-empty links
        .filter(link -> !link.contains("javascript") && !link.contains("*&")) // filter other patterns
        .filter(link -> link.startsWith("http") || link.startsWith("https"))// filter http and https links
        .map(link -> link.trim())
        .distinct() //remove duplicate data
        .forEach(link -> {
          try {
            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestProperty("User-Agent", "curl/7.64.1");
            http.setConnectTimeout(15000);
            http.setReadTimeout(15000);
            http.setRequestMethod("GET"); //this is important, some websites don't allow head request
            System.out.println("link is: " + link + " status code :" + http.getResponseCode());

            if (http.getResponseCode() != 200) {
              brokenLinks.put(link, http.getResponseCode());
            }

            http.disconnect();
          } catch (Exception e) {
            catchedLinks.put(link, e.toString());
          }
        });

    if (!brokenLinks.isEmpty() || !catchedLinks.isEmpty()) {
      System.out.println("\nJob is done. Failed url lists are: \n");
      brokenLinks.forEach((k, v) -> System.out.println("Broken link is : " + k + ", status code : " + v));
      catchedLinks.forEach((k, v) -> System.out.println("Failed link is : " + k + ", error message : " + v));
    }
    else
    {
      System.out.println("\nJob is done. There is no broken links");
    }
  }
}
