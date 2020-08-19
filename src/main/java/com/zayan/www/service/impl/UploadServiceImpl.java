package com.zayan.www.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.UploadFileRequest;
import com.aliyun.oss.model.UploadUdfImageRequest;
import com.zayan.www.constant.enums.ErrorEnum;
import com.zayan.www.exception.UploadException;
import com.zayan.www.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class UploadServiceImpl implements UploadService {

    private static final String ENDPOINT = "";
    private static final String ACCESS_KEY_ID = "";
    private static final String ACCESS_KEY_SECRET = "";
    private static final String BUCKET_NAME = "";

    private String checkFile(MultipartFile multipartFile, List<String> allowSuffix)  {

        String originalFileName = multipartFile.getOriginalFilename();
        if (multipartFile.isEmpty() || Objects.isNull(originalFileName)) {
            log.info("fileUpload_error, 文件为空");
            throw new UploadException(ErrorEnum.FILE_ERROR);
        }

        String suffix = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        if (!allowSuffix.contains(suffix)) {
            log.info("文件后缀名错误：{}", originalFileName);
            throw new UploadException(ErrorEnum.FILE_ERROR);
        }
        return UUID.randomUUID().toString().replace("-", "") + "." + originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    }

    @Override
    public String fileUploadToLocal(MultipartFile multipartFile, List<String> allowSuffix, String dirName) {

        String filePath = "src/main/resources/";
        String format = "/images/products/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = checkFile(multipartFile, allowSuffix);

        File fileDir = new File(filePath + format);
        if (!fileDir.isDirectory()) {
            boolean mkdir = fileDir.mkdirs();
            if (!mkdir) {
                throw new UploadException(ErrorEnum.FILE_UPLOAD_DIR_ERROR);
            }
        }

        try {
            File newFile = new File(fileDir.getAbsolutePath() + File.separator + fileName);
            multipartFile.transferTo(newFile);
            log.info("文件上传成功：{}, pathName:{}", multipartFile.getOriginalFilename(), filePath + format + fileName);
            return format + "/" + fileName;
        } catch (IOException e) {
            log.info("文件上传失败：{}: error:{}", fileName, e.getMessage());
        }
        return null;
    }

    @Override
    public String fileUploadToOss(MultipartFile multipartFile, List<String> allowSuffix, String dirName) {

        String filePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = checkFile(multipartFile, allowSuffix);

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        try {

            ossClient.putObject(BUCKET_NAME, filePath + fileName, multipartFile.getInputStream());

            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);

            URL url = ossClient.generatePresignedUrl(BUCKET_NAME, filePath + fileName, expiration);

            System.out.println(url);

            log.info("文件上传阿里云成功：{}", multipartFile.getOriginalFilename());
            return fileName;
        } catch (IOException ioException) {
            log.info("文件读取失败：{}: error:{}", fileName, ioException.getMessage());
        } catch (OSSException ossException){
            log.info("文件上传阿里云失败：{}: error:{}", fileName, ossException.getMessage());
        } catch (ClientException clientException) {
            System.out.println(clientException);
            log.info("连接阿里云失败：{}: error:{}", fileName, clientException.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return null;
    }
}