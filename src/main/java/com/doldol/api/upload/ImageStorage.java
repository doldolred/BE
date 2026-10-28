package com.doldol.api.upload;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장 (상품 · 후기 사진) — 서버 디스크(app.upload.dir, 운영은 도커 볼륨)에 두고 /uploads/... 로 보여 준다.
 * 파일 이름 · 확장자는 믿지 않는다: 첫 몇 바이트로 진짜 이미지인지 보고, 이름은 무작위로 새로 짓는다.
 */
@Slf4j
@Component
public class ImageStorage {

    public static final String URL_PREFIX = "/uploads/";

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private final Path root;

    public ImageStorage(@Value("${app.upload.dir:./uploads}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        }
        catch (IOException e) {
            throw new UncheckedIOException("업로드 폴더를 만들 수 없습니다: " + root, e);
        }
        log.info("상품 이미지 저장 위치: {}", root);
    }

    public Path root() {return root;}

    /** 상품 이미지 — 저장하고 화면에서 쓸 주소(/uploads/products/202610/...webp)를 돌려준다 */
    public String store(MultipartFile file) {
        return store(file, "products");
    }

    /** folder(products · reviews) 아래 달별 폴더에 저장 — /uploads/{folder}/202610/{uuid}.{ext} */
    public String store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        String ext;
        try (InputStream in = file.getInputStream()) {
            ext = detect(in.readNBytes(12));
        }
        catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_FILE);
        }
        String relative = folder + "/" + YearMonth.now(ZoneId.of("Asia/Seoul")).format(MONTH) + "/" + UUID.randomUUID() + "." + ext;
        Path target = root.resolve(relative).normalize();
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        }
        catch (IOException e) {
            throw new UncheckedIOException("이미지를 저장하지 못했습니다", e);
        }
        return URL_PREFIX + relative;
    }

    /** 파일 앞부분(매직 넘버)으로 형식 판별 — jpg · png · gif · webp 만 */
    private static String detect(byte[] h) {
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "jpg";
        if (h.length >= 8 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "png";
        if (h.length >= 4 && h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8') return "gif";
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F' && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "webp";
        throw new BusinessException(ErrorCode.INVALID_FILE);
    }

}
