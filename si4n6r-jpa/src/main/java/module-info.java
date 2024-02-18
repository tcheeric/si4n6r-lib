module si4n6r.jpa {
    requires lombok;

    requires nostr.api;

    requires com.fasterxml.jackson.annotation;

    requires jakarta.validation;
    requires jakarta.persistence;

    requires org.hibernate.orm.core;
    requires org.mapstruct;
    requires org.postgresql.jdbc;
    requires spring.data.rest.core;

    exports nostr.si4n6r.model;
    exports nostr.si4n6r.model.dto;
    exports nostr.si4n6r.model.util;

    opens nostr.si4n6r.model to org.hibernate.orm.core, spring.core;
}