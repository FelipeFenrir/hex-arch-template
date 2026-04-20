package com.acme.productcatalog.domain.product;

import com.acme.productcatalog.domain.product.decorator.ProductCapability;
import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.shared.vo.Id;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static com.acme.productcatalog.domain.product.ProductErrorConstants.ID_FIELD_ERROR;
import static com.acme.productcatalog.domain.product.ProductErrorConstants.NAME_FIELD_ERROR;
import static com.acme.productcatalog.domain.product.ProductErrorConstants.SURNAME_FIELD_ERROR;

@Builder(setterPrefix = "with")
@EqualsAndHashCode
@ToString
public final class SimpleProduct implements Product {
    private final Id id;
    private final String sourceId;
    private final String name;
    private final String surname;
    private final String description;
    private final ProductKind productKind = ProductKind.TANGIBLE;
    private ProductStatus productStatus;
    private final Set<ProductCapability> productCapabilities;
    private final Instant createdAt;
    private final Instant updatedAt;

    private SimpleProduct(final Id id, final String sourceId, final String name, final String surname,
                          final String description, final ProductStatus productStatus,
                          final Set<ProductCapability> productCapabilities, final Instant createdAt,
                          final Instant updatedAt) {

        //if (basePrice.isNegative()) throw new IllegalArgumentException("Price must be >= 0");
        this.id = Objects.requireNonNull(id, ID_FIELD_ERROR);
        this.sourceId = sourceId;
        this.name = Objects.requireNonNull(name, NAME_FIELD_ERROR);
        this.surname = Objects.requireNonNull(surname, SURNAME_FIELD_ERROR);
        this.description = description;
        this.productStatus = productStatus;
        //this.basePrice = basePrice;
        //this.taxProfile = taxProfile;
        this.productCapabilities = productCapabilities;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SimpleProduct createNew(final String sourceId, final String name, final String surname,
                                          final String description) {
        Instant now = Instant.now();
        return new SimpleProduct(Id.withoutId(), sourceId, name, surname, description, ProductStatus.DRAFT,
                Set.of(), now, now);
    }

    public static SimpleProduct rehydrate(final Id id, final String sourceId, final String name, final String surname,
                                          final String description, final ProductStatus productStatus,
                                          final Set<ProductCapability> productCapabilities, final Instant createdAt,
                                          final Instant updatedAt) {
        return new SimpleProduct(id, sourceId, name, surname, description, productStatus, productCapabilities, createdAt,
                updatedAt);
    }

    public static SimpleProduct rehydrate(final String id, final String sourceId, final String name,
                                          final String surname, final String description,
                                          final ProductStatus productStatus,
                                          final Set<ProductCapability> productCapabilities, final Instant createdAt,
                                          final Instant updatedAt)  {
        return rehydrate(Id.withId(id), sourceId, name, surname, description, productStatus, productCapabilities,
                createdAt, updatedAt);
    }

//    public void nextStatus(final ProductStatus nextStatus, final ProductWorkflow workflow, final ActionContext context) {
//        var transition = workflow.getTransition(this.productStatus, nextStatus);
//
//        for (ProductFlowActions action : transition.actions()) {
//            executeAction(action, context);
//        }
//
//        this.productStatus = nextStatus;
//    }
//
//    private void executeAction(final ProductFlowActions action, final ActionContext context) {
//        switch (action) {
//            case VALIDATE_FIELDS -> context.validator().validate(this);
//            case BUSINESS_TEAM_MANUAL_APPROVAL -> context.approver().waitForApproval(this);
//            case SEND_INTERNAL_COMMUNICATION -> context.emailNotification().sendNotification(this);
//            case SET_PRICE -> context.precificador().configurar(this);
//            case SEND_PRODUCER_COMMUNICATION -> context.eventNotification().publishNotification(this);
//            case SEND_DISTRIBUTOR_COMMUNICATION -> context.eventNotification().publishNotification(this);
//        }
//    }

    @Override public Id id() {
        return id;
    }
    @Override public String sourceId() {
        return sourceId;
    }
    @Override public String name() {
        return name;
    }
    @Override public String surname() {
        return surname;
    }
    @Override public String description() {
        return description;
    }
    @Override public ProductKind productKind() {
        return this.productKind;
    }
    @Override public ProductStatus status() {
        return productStatus;
    }
    @Override public Set<ProductCapability> productCapabilities() {
        return productCapabilities;
    }
    @Override public Instant createdAt() {
        return createdAt;
    }
    @Override public Instant updatedAt() {
        return updatedAt;
    }
}
