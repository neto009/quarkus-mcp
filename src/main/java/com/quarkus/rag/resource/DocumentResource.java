package com.quarkus.rag.resource;

import com.quarkus.rag.entity.Document;
import com.quarkus.rag.repository.DocumentRepository;
import com.quarkus.rag.service.DocumentIngestionService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Path("/api/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

    @Inject
    DocumentRepository documentRepository;

    @Inject
    DocumentIngestionService ingestionService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response uploadDocument(FileUpload file) {
        try {
            // Save document metadata
            Document document = new Document();
            document.setFileName(file.fileName());
            document.setContentType(file.contentType());
            document.setFileSize(file.size());
            document.setUploadedAt(LocalDateTime.now());
            document.setProcessed(false);

            documentRepository.persist(document);

            // Ingest document for RAG
            try (InputStream is = new FileInputStream(file.uploadedFile().toFile())) {
                ingestionService.ingestDocument(is, file.fileName(), file.contentType());
                document.setProcessed(true);
            }

            return Response.ok(document).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error uploading document: " + e.getMessage())
                .build();
        }
    }

    @GET
    public List<Document> listDocuments() {
        return documentRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getDocument(@PathParam("id") Long id) {
        Document document = documentRepository.findById(id);
        if (document == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(document).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteDocument(@PathParam("id") Long id) {
        boolean deleted = documentRepository.deleteById(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}

