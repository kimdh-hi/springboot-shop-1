package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    /* 엔티티를 직접 노출
       엔티티 변경시 API의 스펙이 바뀐다는 단점 외에 컬렉션을 바로 리턴하게 되면 확장성이 떨어짐
           Json기준 [ {..} ] 의 형태 X
       반환되는 컬렉션을 다른 제네릭 오브젝트로 감싸서 반환해야 함.
           Json기준 { [...] } 의 형태 O
     */
    @GetMapping("/api/v1/members")
    public List<Member> findMembersV1() {
        return memberService.findMembers();
    }

    /*
       자바 컬렉션 객체를 제네릭 클래스를 감싸서 반환 함.
       { data : [..] } 의 형태이기 때문에 Json에 카운팅, 합계, 평균 등 필드 추가가 용이함.
     */
    @GetMapping("/api/v2/members")
    public FindMemberResponse<Member> findMemberV2() {
        List<Member> members = memberService.findMembers();
        List<FindMemberDto> collect = members.stream()
                .map(m -> new FindMemberDto(m.getName()))
                .collect(Collectors.toList());
        return new FindMemberResponse(collect.size(), collect);
    }

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.updateMember(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId());
    }

    @Data
    @AllArgsConstructor
    static class FindMemberDto {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class FindMemberResponse<T> {
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
    }

    @Data
    static class UpdateMemberRequest {
        @NotEmpty
        private String name;
    }
}
