import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BrokenLinkCheck {

  private static final String baseUrl = "https://www.sahibinden.com/";
  private static final HashMap<String, Integer> brokenUrls = new HashMap<>();

  @SneakyThrows
  public static void main(String[] args) {
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
            http.setConnectTimeout(5000);
            http.setReadTimeout(5000);
            http.setRequestMethod("GET"); //this is important, some websites don't allow head request
            System.out.println("link is: " + link + " status code :" + http.getResponseCode());

            if (http.getResponseCode() != 200) {
              brokenUrls.put(link, http.getResponseCode());
            }

            http.disconnect();
          } catch (Exception e) {
            e.printStackTrace();
          }
        });

    if (!brokenUrls.isEmpty()) {
      System.out.println("\nJob is done. Failed url lists are: \n");
      brokenUrls.forEach((k, v) -> System.out.println("Broken link is : " + k + ", status code = " + v));
    }
    else
    {
      System.out.println("\n Job is done. There is no broken links");
    }
  }
}
