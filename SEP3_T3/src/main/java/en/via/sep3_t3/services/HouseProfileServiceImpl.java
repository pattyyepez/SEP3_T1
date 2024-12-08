package en.via.sep3_t3.services;

import en.via.sep3_t3.*;
import en.via.sep3_t3.domain.HouseProfile;
import en.via.sep3_t3.repositoryContracts.IHouseProfileRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HouseProfileServiceImpl extends HouseProfileServiceGrpc.HouseProfileServiceImplBase {

  private final IHouseProfileRepository houseProfileRepository;

  public HouseProfileServiceImpl(IHouseProfileRepository houseProfileRepository) {
    this.houseProfileRepository = houseProfileRepository;
  }

  @Override
  public void getHouseProfile(HouseProfileRequest request, StreamObserver<HouseProfileResponse> responseObserver) {
    try {
      HouseProfile houseProfile = houseProfileRepository.findById(request.getId());
      HouseProfileResponse response = getHouseProfileResponse(houseProfile);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllHouseProfiles(AllHouseProfilesRequest request, StreamObserver<AllHouseProfilesResponse> responseObserver) {
    try {
      List<HouseProfile> houseProfiles = houseProfileRepository.findAll();
      List<HouseProfileResponse> houseProfileResponses = new ArrayList<>();

      for(HouseProfile houseProfile : houseProfiles ) {
        houseProfileResponses.add(getHouseProfileResponse(houseProfile));
      }

      AllHouseProfilesResponse response = AllHouseProfilesResponse.newBuilder()
          .addAllHouseProfiles(houseProfileResponses)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void createHouseProfile(CreateHouseProfileRequest request, StreamObserver<HouseProfileResponse> responseObserver) {
    try {
      HouseProfile houseProfile = getHouseProfile(
          0, request.getOwnerId(),
          request.getTitle(), request.getDescription(),
          request.getAddress(), request.getRegion(),
          request.getCity(), request.getAmenitiesList(),
          request.getChoresList(), request.getRulesList(), request.getPicturesList());
      houseProfile.setId(houseProfileRepository.save(houseProfile));
      HouseProfileResponse response = getHouseProfileResponse(houseProfile);

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  @Override
  public void updateHouseProfile(UpdateHouseProfileRequest request, StreamObserver<HouseProfileResponse> responseObserver) {
    try {
      HouseProfile houseProfile = getHouseProfile(request.getId(), 0,
          request.getTitle(), request.getDescription(),
          request.getAddress(), request.getRegion(),
          request.getCity(), request.getAmenitiesList(),
          request.getChoresList(), request.getRulesList(), request.getPicturesList());
      houseProfile.setId(request.getId());
      houseProfile = houseProfileRepository.update(houseProfile);
      HouseProfileResponse response = getHouseProfileResponse(houseProfile);

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  @Override
  public void deleteHouseProfile(HouseProfileRequest request, StreamObserver<HouseProfileResponse> responseObserver) {
    try {
      houseProfileRepository.deleteById(request.getId());
      responseObserver.onNext(HouseProfileResponse.newBuilder().build());
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllRules(AllRulesRequest request, StreamObserver<AllRulesResponse> responseObserver)
  {
    try{
      List<String> rules = houseProfileRepository.findAllRules();
      AllRulesResponse response = AllRulesResponse.newBuilder().addAllRules(rules).build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
    catch (Exception e){
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllChores(AllChoresRequest request, StreamObserver<AllChoresResponse> responseObserver)
  {
    try{
      List<String> chores = houseProfileRepository.findAllChores();
      AllChoresResponse response = AllChoresResponse.newBuilder().addAllChores(chores).build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
    catch (Exception e){
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllAmenities(AllAmenitiesRequest request, StreamObserver<AllAmenitiesResponse> responseObserver)
  {
    try{
      List<String> amenities = houseProfileRepository.findAllAmenities();
      AllAmenitiesResponse response = AllAmenitiesResponse.newBuilder().addAllAmenities(amenities).build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
    catch (Exception e){
      responseObserver.onError(e);
    }
  }

  private static HouseProfile getHouseProfile(int ProfileId, int ownerId, String title, String description, String address,
      String region, String city, List<String> amenities,
      List<String> chores, List<String> rules, List<String> pictures) {
    HouseProfile houseProfile = new HouseProfile();
    houseProfile.setId(ProfileId);
    houseProfile.setOwner_id(ownerId);
    houseProfile.setTitle(title);
    houseProfile.setDescription(description);
    houseProfile.setAddress(address);
    houseProfile.setRegion(region);
    houseProfile.setCity(city);
    houseProfile.setAmenities(amenities);
    houseProfile.setChores(chores);
    houseProfile.setRules(rules);
    houseProfile.setPictures(pictures);
    return houseProfile;
  }

  private static HouseProfileResponse getHouseProfileResponse(HouseProfile houseProfile) {
    return HouseProfileResponse.newBuilder()
        .setId(houseProfile.getId())
        .setOwnerId(houseProfile.getOwner_id())
        .setTitle(houseProfile.getTitle())
        .setDescription(houseProfile.getDescription())
        .setAddress(houseProfile.getAddress())
        .setRegion(houseProfile.getRegion())
        .setCity(houseProfile.getCity())
        .addAllAmenities(houseProfile.getAmenities())
        .addAllChores(houseProfile.getChores())
        .addAllRules(houseProfile.getRules())
        .addAllPictures(houseProfile.getPictures())
        .build();
  }
}
