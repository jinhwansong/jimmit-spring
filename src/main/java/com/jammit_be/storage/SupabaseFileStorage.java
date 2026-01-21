package com.jammit_be.storage;

import com.jammit_be.common.properties.SupabaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.UUID;

@Component
@Profile("!local")
@RequiredArgsConstructor
public class SupabaseFileStorage implements FileStorage {

    private final WebClient supabaseWebClient;
    private final SupabaseProperties supabaseProperties;

    /**
     * MultipartFile을 받아 Supabase Storage에 저장하고, Public URL을 반환한다.
     * @param file 업로드할 파일 (ex: 프로필 이미지)
     * @param subFolder Storage 내 하위 폴더 (ex: "profile" 등, 없으면 root에 저장)
     * @return 저장된 파일의 Public URL
     */
    @Override
    public String save(MultipartFile file, String subFolder) {
        try {
            // 1. 파일명 및 확장자 추출
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new RuntimeException("파일명에 확장자가 없습니다.");
            }
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            // 2. Storage에 저장될 파일명(고유값+확장자) 생성
            String uuid = UUID.randomUUID().toString();
            String filePath = (subFolder != null && !subFolder.isBlank())
                    ? subFolder + "/" + uuid + "." + ext // ex: profile/uuid.png
                    : uuid + "." + ext;  // ex: uuid.png

            // 3. 파일을 바이트 배열로 읽기
            byte[] fileBytes = file.getBytes();

            // 4. Supabase Storage API로 업로드
            String uploadUrl = "/storage/v1/object/" + supabaseProperties.getStorageBucketName() + "/" + filePath;
            
            String contentType = file.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // 4. Supabase Storage API로 업로드 실행
            supabaseWebClient.post()
                    .uri(uploadUrl)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("x-upsert", "true") // 파일이 이미 있으면 덮어쓰기
                    .header("Cache-Control", "3600")
                    .bodyValue(fileBytes)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 5. Public URL 생성 및 반환
            // Supabase Storage의 Public URL 형식: {supabaseUrl}/storage/v1/object/public/{bucket}/{path}
            String publicUrl = supabaseProperties.getUrl() + "/storage/v1/object/public/" + supabaseProperties.getStorageBucketName() + "/" + filePath;
            return publicUrl;

        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Supabase Storage 파일 업로드 실패: " + e.getMessage(), e);
        }
    }
}
