package com.acme.productcatalog.testutils.mocks.serviceobject.insurance;

import com.acme.productcatalog.domain.product.parameterization.ProductStatus;
import com.acme.productcatalog.domain.serviceobject.insurance.InsuredItemType;
import com.acme.shared.stereotypes.test.MockClass;

import static com.acme.productcatalog.testutils.mocks.common.enumerator.ParameterizationStatusMock.ACTIVE_STRING;
import static com.acme.productcatalog.testutils.mocks.common.enumerator.ParameterizationStatusMock.INACTIVE_STRING;
import static com.acme.productcatalog.testutils.mocks.common.enumerator.ParameterizationStatusMock.DRAFT_STRING;
import static com.acme.productcatalog.testutils.mocks.question.QuestionMock.default_question_list;

@MockClass
public class InsuredItemTypeMock {
    public static String ACME_TYPE = "ACME";
    public static String ACME_LABEL = "Acme Insurance type test";

    //Seguros Patrimoniais
    public static String PROPERTY_TYPE = "PROPERTY";
    public static String PROPERTY_LABEL = "Insurance Item of type Property";
    public static String BUSINESS_EQUIPMENT_TYPE = "BUSINESS_EQUIPMENT";
    public static String BUSINESS_EQUIPMENT_LABEL = "Insurance Item of type Business Equipment";
    public static String RURAL_EQUIPMENT_TYPE = "RURAL_EQUIPMENT";
    public static String RURAL_EQUIPMENT_LABEL = "Insurance Item of type Rural Equipment";
    public static String STRUCTURE_TYPE = "STRUCTURE";
    public static String STRUCTURE_LABEL = "Insurance Item of type Structure under construction";

    //Seguros de Veículos
    public static String AUTOMOBILE_TYPE = "AUTOMOBILE";
    public static String AUTOMOBILE_LABEL = "Insurance Item of type Automobiles and Cars";
    public static String MOTORCYCLE_TYPE = "MOTORCYCLE";
    public static String MOTORCYCLE_LABEL = "Insurance Item of type Motorcycles and Motorbikes";
    public static String VESSELS_TYPE = "VESSELS";
    public static String VESSELS_LABEL = "Insurance Item of type Vessels, Boats, Speedboats and Sailboats";
    public static String AIRCRAFT_TYPE = "AIRCRAFT";
    public static String AIRCRAFT_LABEL = "Insurance Item of type Airplanes";

    //Seguros de Pessoas
    public static String LIFE_TYPE = "LIFE";
    public static String LIFE_LABEL = "Insurance Item of type Life of the insured";
    public static String PHYSICAL_INTEGRITY_TYPE = "PHYSICAL_INTEGRITY";
    public static String PHYSICAL_INTEGRITY_LABEL = "Insurance Item of type Physical integrity of the insured";
    public static String FUNERAL_EXPENSES_TYPE = "FUNERAL_EXPENSES";
    public static String FUNERAL_EXPENSES_LABEL = "Insurance Item of type Funeral service costs";
    public static String EDUCATIONAL_TYPE = "EDUCATIONAL";
    public static String EDUCATIONAL_LABEL = "Insurance Item of type Continuity of studies in case of unforeseen circumstances";
    public static String TRAVEL_TYPE = "TRAVEL";
    public static String TRAVEL_LABEL = "Insurance Item of type Traveler and his belongings";

    //Seguros de Transporte e Logística
    //Seguros de Responsabilidade Civil
    //Seguros Rurais
    //Seguros Digitais e Cibernéticos

    public static InsuredItemType ACME = InsuredItemType.builder()
            .withCode(ACME_TYPE)
            .withLabel(ACME_LABEL)
            .withStatus(ProductStatus.fromName(ACTIVE_STRING))
            .withQuestions(default_question_list)
            .build();

    public static InsuredItemType ACME_INACTIVE = InsuredItemType.builder()
            .withCode(ACME_TYPE)
            .withLabel(ACME_LABEL)
            .withStatus(ProductStatus.fromName(INACTIVE_STRING))
            .withQuestions(default_question_list)
            .build();

    public static InsuredItemType ACME_DRAFT = InsuredItemType.builder()
            .withCode(ACME_TYPE)
            .withLabel(ACME_LABEL)
            .withStatus(ProductStatus.fromName(DRAFT_STRING))
            .withQuestions(default_question_list)
            .build();

    private InsuredItemTypeMock() {}
}