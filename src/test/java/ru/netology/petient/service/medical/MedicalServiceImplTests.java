package ru.netology.petient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedicalServiceImplTests {
    @Mock
    private PatientInfoRepository patientInfoRepository;
    @Mock
    private SendAlertService alertService;
    @InjectMocks
    private MedicalServiceImpl medicalService;

    private static Stream<Arguments> provideBloodPressure() {
        return Stream.of(
                Arguments.of(new BloodPressure(120, 80), false),
                Arguments.of(new BloodPressure(140, 90), true),
                Arguments.of(new BloodPressure(100, 60), true)
        );
    }

    private static Stream<Arguments> provideTemperature() {
        return Stream.of(
                Arguments.of(new BigDecimal("36.6"), false),
                Arguments.of(new BigDecimal("37.9"), false),
                Arguments.of(new BigDecimal("34.6"), true)
        );
    }

    @BeforeEach
    void setUp() {
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @ParameterizedTest
    @MethodSource("provideBloodPressure")
    void testCheckBloodPressure_AndMessageInAlertService(BloodPressure checkingPressure, boolean shouldSendAlert) {
        //Arrange
        String patientId = "12";
        PatientInfo patientInfo = new PatientInfo(patientId, "John", " Doe", LocalDate.of(1980, 11, 26), new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80)));
        when(patientInfoRepository.getById(patientId)).thenReturn(patientInfo);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //Act
        medicalService.checkBloodPressure(patientId, checkingPressure);
        //Assert
        if (shouldSendAlert) {
            verify(alertService, times(1)).send(captor.capture());
            String capturedMessage = captor.getValue();
            assertEquals("Warning, patient with id: 12, need help", capturedMessage);
        } else {
            verify(alertService, times(0)).send(anyString());
        }
    }

    @ParameterizedTest
    @MethodSource("provideTemperature")
    void testCheckTemperature_AndMessageInAlertService(BigDecimal checkingTemperature, boolean shouldSendAlert) {
        //should call alertService and send message, if temperature is too low

        //Arrange
        String patientId = "12";
        PatientInfo patientInfo = new PatientInfo(patientId, "John", " Doe", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80)));
        when(patientInfoRepository.getById(patientId)).thenReturn(patientInfo);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        //Act
        medicalService.checkTemperature(patientId, checkingTemperature);
        //Assert
        if (shouldSendAlert) {
            verify(alertService, times(1)).send(captor.capture());
            String capturedMessage = captor.getValue();
            assertEquals("Warning, patient with id: 12, need help", capturedMessage);
        } else {
            verify(alertService, times(0)).send(anyString());
        }
    }
}

