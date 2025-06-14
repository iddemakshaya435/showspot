package com.eventbooking.service;

import com.eventbooking.entity.Booking;
import com.eventbooking.entity.Ticket;
import com.eventbooking.entity.User;
import com.eventbooking.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingService bookingService;

    public List<Ticket> getTicketsByBooking(Long bookingId, User user) {
        Booking booking = bookingService.getBookingById(bookingId, user);
        return ticketRepository.findByBooking(booking);
    }

    public Ticket getTicketByNumber(String ticketNumber, User user) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        if (!ticket.getBooking().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        
        return ticket;
    }

    public void generateTicketsForBooking(Booking booking) {
        List<Ticket> tickets = new ArrayList<>();
        
        for (int i = 0; i < booking.getNumberOfTickets(); i++) {
            String ticketNumber = generateTicketNumber();
            String qrCode = generateQRCode(ticketNumber);
            
            Ticket ticket = new Ticket(ticketNumber, booking, qrCode);
            tickets.add(ticket);
        }
        
        ticketRepository.saveAll(tickets);
    }

    public Resource generateTicketPDF(String ticketNumber, User user) {
        Ticket ticket = getTicketByNumber(ticketNumber, user);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add ticket information
            document.add(new Paragraph("EVENT TICKET"));
            document.add(new Paragraph("Ticket Number: " + ticket.getTicketNumber()));
            document.add(new Paragraph("Event: " + ticket.getBooking().getEvent().getName()));
            document.add(new Paragraph("Date: " + ticket.getBooking().getEvent().getEventDate()));
            document.add(new Paragraph("Venue: " + ticket.getBooking().getEvent().getVenue()));
            document.add(new Paragraph("Location: " + ticket.getBooking().getEvent().getLocation()));
            document.add(new Paragraph("Holder: " + ticket.getBooking().getUser().getFirstName() + 
                                     " " + ticket.getBooking().getUser().getLastName()));
            
            document.close();
            
            return new ByteArrayResource(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }

    public boolean validateTicket(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber).isPresent();
    }

    public void markTicketAsUsed(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        if (ticket.isUsed()) {
            throw new RuntimeException("Ticket already used");
        }
        
        ticket.setUsed(true);
        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
    }

    private String generateTicketNumber() {
        return "TK" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    private String generateQRCode(String ticketNumber) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(ticketNumber, BarcodeFormat.QR_CODE, 200, 200);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage());
        }
    }
}