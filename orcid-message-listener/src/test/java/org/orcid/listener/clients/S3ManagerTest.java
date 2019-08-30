package org.orcid.listener.clients;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.orcid.jaxb.model.common_v2.LastModifiedDate;
import org.orcid.jaxb.model.common_v2.OrcidIdentifier;
import org.orcid.jaxb.model.error_v2.OrcidError;
import org.orcid.jaxb.model.record_v2.History;
import org.orcid.jaxb.model.record_v2.Record;
import org.orcid.jaxb.model.record_v2.Work;
import org.orcid.listener.s3.S3Manager;
import org.orcid.listener.s3.S3MessagingService;
import org.orcid.test.OrcidJUnit4ClassRunner;
import org.orcid.utils.DateUtils;
import org.springframework.test.context.ContextConfiguration;

import com.amazonaws.AmazonClientException;
import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(OrcidJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:orcid-message-listener-test-context.xml" })
public class S3ManagerTest {

    @Mock
    private S3MessagingService s3MessagingService;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void updateS3_V2RecordTest() throws JAXBException, AmazonClientException, IOException {
        String bucketPrefix = "bucket-production";
        String orcid = "0000-0000-0000-000X";
        Record record = new Record();
        record.setOrcidIdentifier(new OrcidIdentifier(orcid));
        Date now = new Date();
        LastModifiedDate lmd = new LastModifiedDate();
        lmd.setValue(DateUtils.convertToXMLGregorianCalendar(now));
        History h = new History();
        h.setLastModifiedDate(lmd);
        record.setHistory(h);
                
        S3Manager s3 = new S3Manager(bucketPrefix);
        s3.setS3MessagingService(s3MessagingService);
        s3.uploadV2RecordSummary(orcid, record);
        verify(s3MessagingService, times(1)).sendV2Item(eq("000/0000-0000-0000-0000/0000-0000-0000-000X.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), eq(now), eq(false));
        verify(s3MessagingService, times(0)).sendV3Item(eq("000/0000-0000-0000-0000/0000-0000-0000-000X.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), eq(now), eq(false));
    }

    @Test
    public void updateS3_V3RecordTest() throws JAXBException, AmazonClientException, IOException {
        String bucketPrefix = "bucket-production";
        String orcid = "0000-0000-0000-000X";
        org.orcid.jaxb.model.v3.release.record.Record record = new org.orcid.jaxb.model.v3.release.record.Record();
        record.setOrcidIdentifier(new org.orcid.jaxb.model.v3.release.common.OrcidIdentifier(orcid));
        Date now = new Date();
        org.orcid.jaxb.model.v3.release.common.LastModifiedDate lmd = new org.orcid.jaxb.model.v3.release.common.LastModifiedDate();
        lmd.setValue(DateUtils.convertToXMLGregorianCalendar(now));
        org.orcid.jaxb.model.v3.release.record.History h = new org.orcid.jaxb.model.v3.release.record.History();
        h.setLastModifiedDate(lmd);
        record.setHistory(h);
        
        S3Manager s3 = new S3Manager(bucketPrefix);
        s3.setS3MessagingService(s3MessagingService);
        s3.uploadV3RecordSummary(orcid, record);
        verify(s3MessagingService, times(0)).sendV2Item(eq("000/0000-0000-0000-0000/0000-0000-0000-000X.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), eq(now), eq(false));
        verify(s3MessagingService, times(1)).sendV3Item(eq("000/0000-0000-0000-0000/0000-0000-0000-000X.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), eq(now), eq(false));
    }
    
    @Test
    public void uploadV2ActivityTest() throws JAXBException, JsonProcessingException {
        String orcid = "0000-0000-0000-0000";
        Date now = new Date();
        LastModifiedDate lmd = new LastModifiedDate();
        lmd.setValue(DateUtils.convertToXMLGregorianCalendar(now));
        Work w = new Work();
        w.setLastModifiedDate(lmd);

        S3Manager s3 = new S3Manager();
        s3.setS3MessagingService(s3MessagingService);
        s3.uploadV2Activity(orcid, "1234", w);
        verify(s3MessagingService, times(1)).sendV2Item(eq("000/0000-0000-0000-0000/works/0000-0000-0000-0000_works_1234.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML),
                eq(now), eq(true));
        verify(s3MessagingService, times(0)).sendV3Item(eq("000/0000-0000-0000-0000/works/0000-0000-0000-0000_works_1234.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML),
                eq(now), eq(true));
    }

    @Test
    public void uploadV3ActivityTest() throws JAXBException, JsonProcessingException {
        String orcid = "0000-0000-0000-0000";
        Date now = new Date();
        org.orcid.jaxb.model.v3.release.common.LastModifiedDate lmd = new org.orcid.jaxb.model.v3.release.common.LastModifiedDate();
        lmd.setValue(DateUtils.convertToXMLGregorianCalendar(now));
        org.orcid.jaxb.model.v3.release.record.Work w = new org.orcid.jaxb.model.v3.release.record.Work();
        w.setLastModifiedDate(lmd);

        S3Manager s3 = new S3Manager();
        s3.setS3MessagingService(s3MessagingService);
        s3.uploadV3Activity(orcid, "1234", w);
        verify(s3MessagingService, times(0)).sendV2Item(eq("000/0000-0000-0000-0000/works/0000-0000-0000-0000_works_1234.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML),
                eq(now), eq(true));
        verify(s3MessagingService, times(1)).sendV3Item(eq("000/0000-0000-0000-0000/works/0000-0000-0000-0000_works_1234.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML),
                eq(now), eq(true));
    }
    
    @Test
    public void uploadV2OrcidErrorTest() throws JAXBException, JsonProcessingException {
        String orcid = "0000-0000-0000-0000";
        OrcidError error = new OrcidError();
        S3Manager s3 = new S3Manager();
        s3.setS3MessagingService(s3MessagingService);
        s3.uploadV2OrcidError(orcid, error);

        verify(s3MessagingService, times(1)).sendV2Item(eq("000/0000-0000-0000-0000.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), any(Date.class), eq(false));
        verify(s3MessagingService, times(0)).sendV2Item(eq("000/0000-0000-0000-0000.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), any(Date.class), eq(false));
    }
    
    @Test
    public void uploadV3OrcidErrorTest() throws JAXBException, JsonProcessingException {
        String orcid = "0000-0000-0000-0000";
        org.orcid.jaxb.model.v3.release.error.OrcidError error = new org.orcid.jaxb.model.v3.release.error.OrcidError();
        S3Manager s3 = new S3Manager();
        s3.setS3MessagingService(s3MessagingService);
        s3.uploadV3OrcidError(orcid, error);

        verify(s3MessagingService, times(0)).sendV2Item(eq("000/0000-0000-0000-0000.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), any(Date.class), eq(false));
        verify(s3MessagingService, times(1)).sendV2Item(eq("000/0000-0000-0000-0000.xml"), any(byte[].class), eq(MediaType.APPLICATION_XML), any(Date.class), eq(false));
    }
}
