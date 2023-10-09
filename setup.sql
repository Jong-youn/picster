create database pinster;

CREATE TABLE pinster.user
(
    id    int unsigned not null primary key AUTO_INCREMENT,
    name  varchar(100),
    account varchar(255) not null,
    password varchar(500) not null,
    unique (id)
);

CREATE TABLE pinster.image
(
    id             int unsigned not null primary key AUTO_INCREMENT,
    user_id        int unsigned not null,
    foreign key (user_id) references user(id),
    title          varchar(300),
    description    varchar(300),
    image_url      varchar(500) not null,
    view_count     INT DEFAULT 0,
    created_at     DATETIME not null DEFAULT CURRENT_TIMESTAMP(),
    updated_at     DATETIME not null DEFAULT CURRENT_TIMESTAMP(),
    deleted_at     DATETIME,
    unique (id)
);
CREATE INDEX created_at ON pinster.image(created_at);
