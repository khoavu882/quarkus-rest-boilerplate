-- Test data for H2 database
-- This script will be loaded during test initialization

-- Create test schema if needed
-- CREATE SCHEMA IF NOT EXISTS sch_local;

-- Insert test data for entities
-- Add your test data here based on your domain entities

-- Example test data (adjust based on your actual entities)
INSERT INTO demo_entity (id, name, created_at) VALUES
(1, 'Test Entity 1', CURRENT_TIMESTAMP),
(2, 'Test Entity 2', CURRENT_TIMESTAMP);

-- Add more test data as needed for your specific entities
