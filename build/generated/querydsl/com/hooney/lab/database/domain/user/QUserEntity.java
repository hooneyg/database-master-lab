package com.hooney.lab.database.domain.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = -217135886L;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final com.hooney.lab.database.domain.common.QBaseTimeEntity _super = new com.hooney.lab.database.domain.common.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final ListPath<com.hooney.lab.database.domain.post.PostEntity, com.hooney.lab.database.domain.post.QPostEntity> posts = this.<com.hooney.lab.database.domain.post.PostEntity, com.hooney.lab.database.domain.post.QPostEntity>createList("posts", com.hooney.lab.database.domain.post.PostEntity.class, com.hooney.lab.database.domain.post.QPostEntity.class, PathInits.DIRECT2);

    public final EnumPath<UserStatus> status = createEnum("status", UserStatus.class);

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

