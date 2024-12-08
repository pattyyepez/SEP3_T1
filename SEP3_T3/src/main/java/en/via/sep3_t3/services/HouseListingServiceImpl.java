package en.via.sep3_t3.services;

import en.via.sep3_t3.*;
import en.via.sep3_t3.domain.HouseListing;
import en.via.sep3_t3.repositoryContracts.IHouseListingRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class HouseListingServiceImpl extends HouseListingServiceGrpc.HouseListingServiceImplBase {

  private final IHouseListingRepository houseListingRepository;

  public HouseListingServiceImpl(IHouseListingRepository houseListingRepository) {
    this.houseListingRepository = houseListingRepository;
  }

  @Override
  public void getHouseListing(HouseListingRequest request, StreamObserver<HouseListingResponse> responseObserver) {
    try {
      HouseListing houseListing = houseListingRepository.findById(request.getId());
      HouseListingResponse response = buildHouseListingResponse(houseListing);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void getAllHouseListings(AllHouseListingsRequest request, StreamObserver<AllHouseListingsResponse> responseObserver) {
    try {
      List<HouseListing> houseListings = houseListingRepository.findAll();
      List<HouseListingResponse> houseListingResponses = new ArrayList<>();

      for(HouseListing houseListing : houseListings ) {
        houseListingResponses.add(buildHouseListingResponse(houseListing));
      }

      AllHouseListingsResponse response = AllHouseListingsResponse.newBuilder()
          .addAllHouseListings(houseListingResponses)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  @Override
  public void createHouseListing(CreateHouseListingRequest request, StreamObserver<HouseListingResponse> responseObserver) {
    try {
      HouseListing houseListing = new HouseListing();
      houseListing.setProfile_id(request.getProfileId());
      houseListing.setStartDate(new Date(request.getStartDate()));
      houseListing.setEndDate(new Date(request.getEndDate()));
      houseListing.setStatus(request.getStatus());

      int id = houseListingRepository.save(houseListing);
      houseListing.setId(id);

      HouseListingResponse response = buildHouseListingResponse(houseListing);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void updateHouseListing(UpdateHouseListingRequest request, StreamObserver<HouseListingResponse> responseObserver) {
    try {
      HouseListing houseListing = new HouseListing();
      houseListing.setId(request.getId());
      houseListing.setStatus(request.getStatus());

      houseListing = houseListingRepository.update(houseListing);
      HouseListingResponse response = buildHouseListingResponse(houseListing);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void deleteHouseListing(HouseListingRequest request, StreamObserver<HouseListingResponse> responseObserver) {
    try {
      houseListingRepository.deleteById(request.getId());
      HouseListingResponse response = HouseListingResponse.newBuilder().build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  private HouseListingResponse buildHouseListingResponse(HouseListing houseListing) {
    return HouseListingResponse.newBuilder()
        .setId(houseListing.getId())
        .setProfileId(houseListing.getProfile_id())
        .setStartDate(houseListing.getStartDate() != null ? houseListing.getStartDate().getTime() : 0)
        .setEndDate(houseListing.getEndDate() != null ? houseListing.getEndDate().getTime() : 0)
        .setStatus(houseListing.getStatus())
        .build();
  }
}
