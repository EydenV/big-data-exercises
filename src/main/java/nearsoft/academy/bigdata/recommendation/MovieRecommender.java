package nearsoft.academy.bigdata.recommendation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.nio.file.Files;

public class MovieRecommender {

    public File resource;
    public File data;
    private String pathToResources = "src/main/resources/data.txt";

    public int totalReviews;
    public long totalProducts;
    public long totalUsers;

    private HashMap<String,Long> userHash = new HashMap<String, Long>();
    private HashMap<String, Long> productHash = new HashMap<String, Long>();
    private HashMap<Long, String> productHashStringId = new HashMap<Long, String>();


    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }


    public List getRecommendationsForUser(String user) throws TasteException, IOException {

        DataModel model = new FileDataModel(data);

        UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

        UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

        UserBasedRecommender recommender = new GenericUserBasedRecommender(model,neighborhood,similarity);

        List<RecommendedItem> recommendations = recommender.recommend(userHash.get(user), 3);
        List<String> recommendationIds = new ArrayList<String>();

        for (RecommendedItem recommendation : recommendations) {
            long itemId = recommendation.getItemID();
            recommendationIds.add(productHashStringId.get(itemId));
        }

        return recommendationIds;

    }

    private URL getURL(String filename){
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(filename);
        return url;
    }

    private File getFileFromResources(String fileName) {
        URL resource = getURL(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }

    public void setInfo() throws IOException {

        if (resource != null) {
            FileReader reader = new FileReader(resource);
            BufferedReader br = new BufferedReader(reader);

            data = new File(pathToResources);

            Files.deleteIfExists(data.toPath());
            FileWriter fw = new FileWriter(data,true);
            BufferedWriter nbr = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(nbr);

            String line;

            String userId = "";
            String productId = "";
            String score = "";

            while ((line = br.readLine()) != null) {

                if (line.startsWith("product/productId")) {
                    totalReviews++;
                    productId = line.split(" ")[1];

                    if (!productHash.containsKey(productId)) {
                        totalProducts++;
                        productHash.put(productId, totalProducts);
                        productHashStringId.put(totalProducts,productId);
                    }
                }
                if (line.startsWith("review/userId")) {
                    userId = line.split(" ")[1];

                    if (!userHash.containsKey(userId)) {
                        totalUsers++;
                        userHash.put(userId, totalUsers);
                    }
                }
                if (line.startsWith("review/score:")) {
                    score = line.split(" ")[1];
                    pw.println(userHash.get(userId) + "," + productHash.get(productId) + "," + score);
                }
            }

            totalProducts = productHash.size();
            totalUsers = userHash.size();
            br.close();
            pw.close();
        }
    }

    public MovieRecommender(String path) throws IOException, TasteException {
        this.resource = getFileFromResources(path);
        setInfo();
    }

    public static void main (String [] args) throws IOException, TasteException {
        MovieRecommender movie = new MovieRecommender("movies.txt");

        System.out.println("REVIEWS " + movie.totalReviews);
        System.out.println("PRODUCTS " + movie.totalProducts);
        System.out.println("USERS " + movie.totalUsers);


    }
}
