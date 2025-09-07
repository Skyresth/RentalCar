-- Customers
CREATE TABLE customers (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  points INT NOT NULL DEFAULT 0
);

-- Cars
CREATE TABLE cars (
  id BIGSERIAL PRIMARY KEY,
  brand VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL,
  available BOOLEAN NOT NULL
);

-- Rentals
CREATE TABLE rentals (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  car_id BIGINT NOT NULL,
  type VARCHAR(50) NOT NULL,
  start_date DATE NOT NULL,
  days_booked INT NOT NULL,
  prepaid_amount DOUBLE PRECISION NOT NULL,
  status VARCHAR(50) NOT NULL,
  CONSTRAINT fk_rental_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_rental_car FOREIGN KEY (car_id) REFERENCES cars(id)
);
CREATE INDEX idx_rentals_status ON rentals(status);
CREATE INDEX idx_rentals_customer ON rentals(customer_id);
