CREATE TABLE category (
  id UUID,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE users (
  id UUID,
  username varchar(100) NOT NULL,
  password varchar(255) NOT NULL,
  activationCode varchar(255) NOT NULL,
  isActive boolean NOT NULL,
  last_login timestamp with time zone NOT NULL,
  created_at timestamp DEFAULT current_timestamp,
  updated_at timestamp DEFAULT NULL ON UPDATE current_timestamp,
  image_url varchar(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE product (
  id UUID,
  name varchar(100) NOT NULL,
  description varchar(255) NOT NULL,
  rating numeric(2,1) CHECK (rating >= 0 AND rating <=5),
  price decimal(10,2) NOT NULL,
  stock decimal(10,2) NOT NULL,
  created_at timestamp DEFAULT current_timestamp,
  updated_at timestamp DEFAULT NULL ON UPDATE current_timestamp,
  image_url varchar(255) NOT NULL,
  created_by UUID,
  category_id UUID,
  PRIMARY KEY (id),
  CONSTRAINT FK_product.category_id
    FOREIGN KEY (category_id)
      REFERENCES category(id),
  CONSTRAINT FK_product.created_by
    FOREIGN KEY (created_by)
      REFERENCES users(id)
);

CREATE TABLE donator (
  id UUID,
  name varchar(100) NOT NULL,
  email varchar(100) NOT NULL,
  amount_donated decimal(10,2) NOT NULL,
  donation_date timestamp with time zone NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE donation (
  id UUID,
  status varchar(20) NOT NULL,
  goal decimal(10,2) NOT NULL,
  amount_received decimal(10,2) NOT NULL,
  donation_date timestamp with time zone NOT NULL,
  donator_id UUID,
  PRIMARY KEY (id),
  CONSTRAINT FK_donation.donator_id
    FOREIGN KEY (donator_id)
      REFERENCES donator(id)
);

CREATE TABLE roles (
  id UUID NOT NULL,
  name varchar(20) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE user_roles (
  user_id UUID NOT NULL,
  role_id UUID NOT NULL,
  CONSTRAINT FK_user_roles.user_id
    FOREIGN KEY (user_id)
      REFERENCES users(id),
  CONSTRAINT FK_user_roles.role_id
    FOREIGN KEY (role_id)
      REFERENCES roles(id)
);

CREATE TABLE purchase (
  id UUID,
  quantity decimal(10,2) NOT NULL,
  unit_price decimal(10,2) NOT NULL,
  product_id UUID,
  PRIMARY KEY (id),
  CONSTRAINT FK_purchase.product_id
    FOREIGN KEY (product_id)
      REFERENCES product(id)
);

CREATE TABLE refreshtoken (
  id UUID,
  expiry_date timestamp NOT NULL,
  token varchar(255) NOT NULL,
  user_id UUID,
  PRIMARY KEY (id),
  CONSTRAINT FK_refreshtoken.user_id
    FOREIGN KEY (user_id)
      REFERENCES users(id)
);

CREATE TABLE survey_question (
  id UUID,
  question varchar(255) NOT NULL,
  response varchar(255) NOT NULL,
  survey_date timestamp DEFAULT current_timestamp,
  donation_id UUID,
  donator_id UUID,
  PRIMARY KEY (id),
  CONSTRAINT FK_survey_question.donation_id
    FOREIGN KEY (donation_id)
      REFERENCES donation(id),
  CONSTRAINT FK_survey_question.donator_id
    FOREIGN KEY (donator_id)
      REFERENCES donator(id)
);

CREATE TABLE user_purchase (
  user_id UUID,
  purchase_id UUID,
  total_price decimal(10,2) NOT NULL,
  purchase_status varchar(20) NOT NULL,
  purchase_date timestamp with time zone NOT NULL,
  CONSTRAINT FK_user_purchase.user_id
    FOREIGN KEY (user_id)
      REFERENCES users(id),
  CONSTRAINT FK_user_purchase.purchase_id
    FOREIGN KEY (purchase_id)
      REFERENCES purchase(id)
);
