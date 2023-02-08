ALTER TABLE users ADD COLUMN telefone varchar(15);


CREATE TABLE email_change_request (
  id UUID,
  new_email varchar(100) NOT NULL,
  confirmed boolean NOT NULL,
  user_id UUID,
  PRIMARY KEY (id),
  CONSTRAINT FK_email_change_request_user_id
    FOREIGN KEY (user_id)
      REFERENCES users(id)
);