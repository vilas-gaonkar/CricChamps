package cric.champs.service.cloud;

import java.io.IOException;
import java.util.Map;

public interface ImageUploadInterface {
    Map uploadImage(Object photo, Map options) throws IOException;
}
