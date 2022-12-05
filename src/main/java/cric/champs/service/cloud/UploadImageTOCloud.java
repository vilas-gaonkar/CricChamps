package cric.champs.service.cloud;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("rawtypes")
@Service
public class UploadImageTOCloud implements ImageUploadInterface {

    private final Cloudinary cloudinary;

    public UploadImageTOCloud() {
        cloudinary = Singleton.getCloudinary();
        cloudinary.config.cloudName = "dpfg6fahb";
        cloudinary.config.apiSecret = "W42gSBgba4pxhoDRl3hESrBZfiA";
        cloudinary.config.apiKey = "825946183238151";
    }

    @Override
    public Map uploadImage(Object photo, Map options) throws IOException {
        return cloudinary.uploader().upload(photo, options);
    }
}
