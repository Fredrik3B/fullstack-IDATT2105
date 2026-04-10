package edu.ntnu.idatt2105.backend.document.mapper;

import edu.ntnu.idatt2105.backend.document.dto.DocumentDto;
import edu.ntnu.idatt2105.backend.document.model.DocumentModel;
import org.springframework.stereotype.Component;

/**
 * Maps {@link DocumentModel} entities to {@link DocumentDto} response objects.
 */
@Component
public class DocumentMapper {

  /**
   * Maps a {@link DocumentModel} to a {@link DocumentDto}.
   *
   * @param doc the document entity
   * @return the document DTO with the uploader's full name resolved
   */
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
