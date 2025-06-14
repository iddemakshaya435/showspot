-- Insert sample events
INSERT INTO events (name, description, event_date, venue, location, ticket_price, total_tickets, available_tickets, image_url, status, created_at, updated_at) VALUES
('Summer Music Festival', 'A fantastic outdoor music festival featuring top artists from around the world.', '2024-07-15 18:00:00', 'Central Park Amphitheater', 'New York, NY', 75.00, 5000, 5000, 'https://images.pexels.com/photos/1105666/pexels-photo-1105666.jpeg', 'ACTIVE', NOW(), NOW()),
('Tech Conference 2024', 'Annual technology conference with keynote speakers and workshops.', '2024-06-20 09:00:00', 'Convention Center', 'San Francisco, CA', 150.00, 1000, 1000, 'https://images.pexels.com/photos/2774556/pexels-photo-2774556.jpeg', 'ACTIVE', NOW(), NOW()),
('Food & Wine Expo', 'Taste the finest cuisines and wines from local and international vendors.', '2024-08-10 12:00:00', 'Grand Exhibition Hall', 'Chicago, IL', 45.00, 2000, 2000, 'https://images.pexels.com/photos/1267320/pexels-photo-1267320.jpeg', 'ACTIVE', NOW(), NOW()),
('Art Gallery Opening', 'Exclusive opening of contemporary art exhibition featuring emerging artists.', '2024-05-25 19:00:00', 'Modern Art Museum', 'Los Angeles, CA', 25.00, 300, 300, 'https://images.pexels.com/photos/1839919/pexels-photo-1839919.jpeg', 'ACTIVE', NOW(), NOW()),
('Sports Championship', 'Championship game featuring the top teams of the season.', '2024-09-05 15:00:00', 'Stadium Arena', 'Miami, FL', 120.00, 8000, 8000, 'https://images.pexels.com/photos/274422/pexels-photo-274422.jpeg', 'ACTIVE', NOW(), NOW());

-- Insert admin user (password: admin123)
INSERT INTO users (first_name, last_name, email, password, phone_number, role, is_enabled, created_at, updated_at) VALUES
('Admin', 'User', 'admin@eventbooking.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567890', 'ADMIN', true, NOW(), NOW());