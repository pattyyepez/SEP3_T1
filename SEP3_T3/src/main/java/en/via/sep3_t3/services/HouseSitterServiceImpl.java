package en.via.sep3_t3.services;

import en.via.sep3_t3.*;
import en.via.sep3_t3.domain.HouseSitter;
import en.via.sep3_t3.repositoryContracts.IHouseSitterRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HouseSitterServiceImpl extends HouseSitterServiceGrpc.HouseSitterServiceImplBase {

  private final IHouseSitterRepository houseSitterRepository;

  public HouseSitterServiceImpl(IHouseSitterRepository houseSitterRepository) {
    this.houseSitterRepository = houseSitterRepository;
  }

  @Override
  public void getHouseSitter(HouseSitterRequest request, StreamObserver<HouseSitterResponse> responseObserver) {
    try {
      HouseSitter houseSitter = houseSitterRepository.findById(request.getId());
      HouseSitterResponse response = getHouseSitterResponse(houseSitter);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllHouseSitters(AllHouseSittersRequest request, StreamObserver<AllHouseSittersResponse> responseObserver) {
    try {
      List<HouseSitter> houseSitters = houseSitterRepository.findAll();
      List<HouseSitterResponse> houseSitterResponses = new ArrayList<>();

      for(HouseSitter houseSitter : houseSitters ) {
        houseSitterResponses.add(getHouseSitterResponse(houseSitter));
      }

      AllHouseSittersResponse response = AllHouseSittersResponse.newBuilder()
          .addAllHouseSitters(houseSitterResponses)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void createHouseSitter(CreateHouseSitterRequest request, StreamObserver<HouseSitterResponse> responseObserver) {
    try {
      HouseSitter houseSitter = getHouseSitter(
          request.getName(), request.getEmail(),
          request.getPassword(), request.getProfilePicture(),
          request.getCPR(), request.getPhone(),
          false, 0,
          request.getExperience(), request.getBiography(),
          request.getSkillsList().stream().toList(),
          request.getPicturesList().stream().toList());
      houseSitter.setUserId(houseSitterRepository.save(houseSitter));

      HouseSitterResponse response = getHouseSitterResponse(houseSitter);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  @Override
  public void updateHouseSitter(UpdateHouseSitterRequest request, StreamObserver<HouseSitterResponse> responseObserver) {
    try {
      HouseSitter houseSitter = getHouseSitter(
          request.getName(), request.getEmail(),
          request.getPassword(), request.getProfilePicture(),
          request.getCPR(), request.getPhone(),
          request.getIsVerified(), request.getAdminId(),
          request.getExperience(), request.getBiography(),
          request.getSkillsList().stream().toList(),
          request.getPicturesList().stream().toList());
      houseSitter.setUserId(request.getId());

      houseSitterRepository.update(houseSitter);
      HouseSitterResponse response = getHouseSitterResponse(houseSitter);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void deleteHouseSitter(HouseSitterRequest request, StreamObserver<HouseSitterResponse> responseObserver) {
    try {
      houseSitterRepository.deleteById(request.getId());
      HouseSitterResponse response = HouseSitterResponse.newBuilder().build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllSkills(AllSkillsRequest request, StreamObserver<AllSkillsResponse> responseObserver){
    try{
      List<String> skills = houseSitterRepository.findAllSkills();
      AllSkillsResponse response = AllSkillsResponse.newBuilder().addAllSkill(skills).build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
    catch (Exception e){
      responseObserver.onError(e);
    }
  }

  private static HouseSitter getHouseSitter(String name, String email, String password, String profilePicture, String cpr,
      String phone, boolean isVerified, int adminId, String experience, String biography, List<String> skills, List<String> pictures) {
    HouseSitter houseSitter = new HouseSitter();
    houseSitter.setName(name);
    houseSitter.setEmail(email);
    houseSitter.setPassword(password);
    houseSitter.setProfilePicture(profilePicture);
    houseSitter.setCPR(cpr);
    houseSitter.setPhone(phone);
    houseSitter.setVerified(isVerified);
    houseSitter.setAdminId(adminId);
    houseSitter.setExperience(experience);
    houseSitter.setBiography(biography);
    houseSitter.setSkills(skills);
    houseSitter.setPictures(pictures);
    return houseSitter;
  }

  private static HouseSitterResponse getHouseSitterResponse(HouseSitter houseSitter) {
    return HouseSitterResponse.newBuilder()
        .setId(houseSitter.getUserId())
        .setName(houseSitter.getName())
        .setEmail(houseSitter.getEmail())
        .setPassword(houseSitter.getPassword())
        .setProfilePicture(houseSitter.getProfilePicture())
        .setCPR(houseSitter.getCPR())
        .setPhone(houseSitter.getPhone())
        .setIsVerified(houseSitter.isVerified())
        .setAdminId(houseSitter.getAdminId())
        .setExperience(houseSitter.getExperience())
        .setBiography(houseSitter.getBiography())
        .addAllPictures(houseSitter.getPictures())
        .addAllSkills(houseSitter.getSkills())
        .build();
  }
}
