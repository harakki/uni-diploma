package dev.harakki.comics.media.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MediaUrlProvider {

    /**
     * Get the public URL for an object in S3 by its key.
     *
     * @param s3Key the key of the object in S3
     * @return the public URL for accessing the object
     */
    String getPublicUrl(String s3Key);

    /**
     * Get the public URL for a media object by its UUID.
     *
     * @param mediaId the UUID of the media object
     * @return the public URL for accessing the media object
     */
    String getPublicUrl(UUID mediaId);

    /**
     * Get the public URLs for multiple objects in S3 by their keys.
     *
     * @param s3Keys the list of keys for the objects in S3
     * @return a map with S3 keys as keys and their corresponding public URLs as values
     */
    Map<String, String> getPublicUrls(List<String> s3Keys);

}
