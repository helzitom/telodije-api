package pe.telodije.api.multimedia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pe.telodije.api.identidad.ErrorNegocio;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@Profile("prod")
public class AlmacenFotosS3 implements AlmacenFotos {

    private final S3Client s3;
    private final String bucket;

    public AlmacenFotosS3(
            @Value("${almacenamiento.s3.endpoint}") String endpoint,
            @Value("${almacenamiento.s3.region}") String region,
            @Value("${almacenamiento.s3.bucket}") String bucket,
            @Value("${almacenamiento.s3.clave-id}") String claveId,
            @Value("${almacenamiento.s3.clave-secreta}") String claveSecreta) {
        this.bucket = bucket;
        this.s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(claveId, claveSecreta)))
                .forcePathStyle(true)
                .build();
    }

    @Override
    public void guardar(String clave, byte[] contenido) {
        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(clave)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(contenido));
        } catch (SdkException e) {
            throw new ErrorNegocio(HttpStatus.SERVICE_UNAVAILABLE,
                    "No pudimos guardar la foto. Intenta de nuevo.");
        }
    }

    @Override
    public boolean existe(String clave) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(clave).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (SdkException e) {
            return false;
        }
    }

    @Override
    public String sha256(String clave) {
        try {
            byte[] bytes = s3.getObjectAsBytes(
                            GetObjectRequest.builder().bucket(bucket).key(clave).build())
                    .asByteArray();
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException | SdkException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la foto", e);
        }
    }
}