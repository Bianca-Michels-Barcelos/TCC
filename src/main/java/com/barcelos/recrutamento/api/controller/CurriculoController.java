package com.barcelos.recrutamento.api.controller;

import com.barcelos.recrutamento.core.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/curriculos")
public class CurriculoController {

    @Value("${app.curriculos.diretorio:./storage/curriculos}")
    private String diretorioCurriculos;

    @Value("${app.curriculos-externos.diretorio:./storage/curriculos-externos}")
    private String diretorioCurriculosExternos;

    
    @PreAuthorize("hasAnyRole('RECRUTADOR', 'ADMIN', 'CANDIDATO')")
    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadCurriculo(@RequestParam String path) {
        try {

            String baseDir;
            String fileName;
            
            if (path.startsWith("curriculos-externos/") || path.startsWith("./storage/curriculos-externos/")) {

                baseDir = diretorioCurriculosExternos;
                if (path.startsWith("curriculos-externos/")) {
                    fileName = path.substring("curriculos-externos/".length());
                } else if (path.startsWith("./storage/curriculos-externos/")) {
                    fileName = path.substring("./storage/curriculos-externos/".length());
                } else {
                    fileName = path;
                }
            } else {

                baseDir = diretorioCurriculos;
                if (path.startsWith(diretorioCurriculos + "/")) {
                    fileName = path.substring((diretorioCurriculos + "/").length());
                } else if (path.startsWith("storage/curriculos/")) {
                    fileName = path.substring("storage/curriculos/".length());
                } else if (path.startsWith("./storage/curriculos/")) {
                    fileName = path.substring("./storage/curriculos/".length());
                } else {
                    fileName = path;
                }
            }

            Path filePath = Paths.get(baseDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Currículo não encontrado: " + fileName);
            }

            String contentType = "application/octet-stream";
            if (fileName.endsWith(".md")) {
                contentType = "text/markdown";
            } else if (fileName.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (fileName.endsWith(".html")) {
                contentType = "text/html";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            throw new ResourceNotFoundException("Erro ao baixar currículo: " + e.getMessage());
        }
    }
}
