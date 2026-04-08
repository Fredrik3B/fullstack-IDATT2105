package edu.ntnu.idatt2105.backend.document.mapper;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDto;
import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

  public DocumentDto toDto(DocumentModel doc) {
    String uploadedByName = doc.getUploadedBy().getFirstName()
        + " " + doc.getUploadedBy().getLastName();
    return new DocumentDto(
        doc.getId(),
        doc.getName(),
        doc.getDescription(),
        doc.getCategory(),
        doc.getModule(),
        doc.getExternalUrl(),
        doc.getOriginalFileName(),
        doc.getFileType(),
        doc.getFileSize(),
        doc.getExpiryDate(),
        doc.getUploadedAt(),
        uploadedByName
    );
  }
}
