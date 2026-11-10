package com.bank.MaverickBank.service;

import com.bank.MaverickBank.dto.request.BranchReqDto;
import com.bank.MaverickBank.dto.response.BranchFilterDto;
import com.bank.MaverickBank.dto.response.BranchRespDto;
import com.bank.MaverickBank.model.Branch;
import com.bank.MaverickBank.repository.BranchRepository;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class BranchServiceTest {
    @InjectMocks
    private BranchService branchService;
    @Mock
    private BranchRepository branchRepository;

    @Test
    public void addBranchTest(){
        BranchReqDto branchReqDto = new BranchReqDto("Chennai Main","MAVB0000300");
        Branch savedBranch = new Branch();
        savedBranch.setId(1L);
        savedBranch.setBranchName("Chennai Main");
        savedBranch.setIfscCode("MAVB0000300");
        savedBranch.setAddedAt(LocalDate.of(2026,11,10));

        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

        BranchRespDto result = branchService.addBranch(branchReqDto);

        assertEquals(1L,result.branchId());
        assertEquals("Chennai Main",result.branchName());
        assertEquals("MAVB0000300",result.ifscCode());
        assertEquals(LocalDate.of(2026,11,10),result.addedAt());

        //the values of the dto must be copied in to the branch that is saved
        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository,times(1)).save(captor.capture());
        assertEquals("Chennai Main",captor.getValue().getBranchName());
        assertEquals("MAVB0000300",captor.getValue().getIfscCode());

        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void addBranchesInBatchTest(){
        List<BranchReqDto> list = List.of(new BranchReqDto("Chennai Main","MAVB0000300"),
                                          new BranchReqDto("Madurai","MAVB0000400"));
        Branch branch1 = new Branch();
        branch1.setId(1L);
        branch1.setBranchName("Chennai Main");
        branch1.setIfscCode("MAVB0000300");
        Branch branch2 = new Branch();
        branch2.setId(2L);
        branch2.setBranchName("Madurai");
        branch2.setIfscCode("MAVB0000400");

        when(branchRepository.saveAll(anyList())).thenReturn(List.of(branch1,branch2));

        List<BranchRespDto> result = branchService.addBranchesInBatch(list);

        assertEquals(2,result.size());
        assertEquals("Chennai Main",result.get(0).branchName());
        assertEquals("MAVB0000400",result.get(1).ifscCode());

        verify(branchRepository,times(1)).saveAll(anyList());

        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void getBranchByIfscCodeTest(){
        Branch branch = new Branch();
        branch.setId(1L);
        branch.setIfscCode("MAVB0000300");

        when(branchRepository.findBranchByIfscCode("MAVB0000300")).thenReturn(branch);

        Branch result = branchService.getBranchByIfscCode("MAVB0000300");

        assertEquals(1L,result.getId());
        assertEquals("MAVB0000300",result.getIfscCode());

        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0000300");

        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void getBranchByIfscCodeTest_Invalid(){
        //unknown IFSC code gives null, the caller has to decide what to do with it
        when(branchRepository.findBranchByIfscCode("MAVB0009999")).thenReturn(null);

        assertNull(branchService.getBranchByIfscCode("MAVB0009999"));

        verify(branchRepository,times(1)).findBranchByIfscCode("MAVB0009999");

        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void dynamicFilterBranchTest(){
        BranchRespDto dto1 = new BranchRespDto(1L,"Chennai Main","MAVB0000300",LocalDate.of(2026,10,1));
        BranchRespDto dto2 = new BranchRespDto(2L,"Chennai South","MAVB0000301",LocalDate.of(2026,10,2));
        LocalDate addedFrom = LocalDate.of(2026,10,1);
        LocalDate addedTill = LocalDate.of(2026,10,31);

        when(branchRepository.dynamicFilterBranch(null,"Chennai","MAVB",addedFrom,addedTill,PageRequest.of(0,5)))
                .thenReturn(new PageImpl<>(List.of(dto1,dto2),PageRequest.of(0,5),2));

        BranchFilterDto result = branchService.dynamicFilterBranch(null,"Chennai","MAVB",addedFrom,addedTill,0,5);

        assertEquals(2,result.list().size());
        assertEquals("Chennai Main",result.list().get(0).branchName());
        assertEquals(1,result.paginationInfo().totalPages());
        assertEquals(2L,result.paginationInfo().totalRecords());
        assertEquals(2,result.paginationInfo().numRecordsInPage());

        verify(branchRepository,times(1))
                .dynamicFilterBranch(null,"Chennai","MAVB",addedFrom,addedTill,PageRequest.of(0,5));

        verifyNoMoreInteractions(branchRepository);
    }

    @Test
    public void dynamicFilterBranchTest_Invalid(){
        //page number can not be negative, so the repository is never reached
        assertThrows(IllegalArgumentException.class,
                ()->branchService.dynamicFilterBranch(null,null,null,null,null,-1,5));

        verifyNoMoreInteractions(branchRepository);
    }
}
