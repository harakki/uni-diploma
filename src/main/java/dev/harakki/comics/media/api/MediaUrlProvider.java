package dev.harakki.comics.media.api;

import java.util.List;
import java.util.Map;

public interface MediaUrlProvider {

    /**
     * Get the public URL for an object in S3 by its key.
     *
     * @param s3Key the key of the object in S3
     * @return the public URL for accessing the object
     */
    String getPublicUrl(String s3Key);

    /**
     * Get the public URLs for multiple objects in S3 by their keys.
     *
     * @param s3Keys the list of keys for the objects in S3
     * @return a map with S3 keys as keys and their corresponding public URLs as values
     */
    Map<String, String> getPublicUrls(List<String> s3Keys);

}
