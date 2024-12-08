﻿using DTOs.Application;
using DTOs.HouseListing;
using DTOs.HouseProfile;
using DTOs.HouseSitter;
using Microsoft.AspNetCore.Mvc;
using RepositoryContracts;

namespace RESTAPI.Controllers;

[ApiController]
[Route("api/[controller]/[action]")]
public class HouseListingController : ControllerBase
{
    private readonly IHouseListingRepository _repo;

    public HouseListingController(IHouseListingRepository repo)
    {
        _repo = repo;
    }

    // Get: api/HouseListing?includeProfile=true
    [HttpGet]
    public async Task<IActionResult> GetAllHouseListings(
        [FromServices] IHouseProfileRepository profileRepo,
        [FromQuery] bool includeProfile)
    {
        try
        {
            var response = _repo.GetAll();
            if (!includeProfile) return Ok(response);
            
            var toReturn = new List<HouseListingDto>();
            foreach (var listing in response)
            {
                listing.Profile = await profileRepo.GetSingleAsync(listing.ProfileId);
                toReturn.Add(listing);
            }
            
            return Ok(toReturn.AsQueryable());
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Error fetching all HouseListing: {ex.Message}\n{ex.InnerException}\n{ex.StackTrace}");
        }
    }
        
    // GET: api/HouseListing/{id}?includeProfile=true
    [HttpGet("{id}")]
    public async Task<IActionResult> GetHouseListing(int id,
        [FromServices] IHouseProfileRepository profileRepo,
        [FromQuery] bool includeProfile)
    {
        try
        {
            var response = await _repo.GetSingleAsync(id);

            if (includeProfile)
                response.Profile = await profileRepo.GetSingleAsync(response.ProfileId);
            
            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Error fetching HouseListing: {ex.Message}\n{ex.InnerException}\n{ex.StackTrace}");
                
        }
    }
    
    [HttpGet("ProfileId")]
    public async Task<IActionResult> GetListingsByProfile([FromQuery] int? profileId)
    {
        try
        {
            IQueryable<HouseListingDto> listings = _repo.GetAll();

            if (profileId.HasValue)
            {
                listings = listings.Where(l => l.ProfileId == profileId.Value);
            }

            return Ok(listings);
        }
        catch (Exception ex)
        {
            return StatusCode(500,
                $"Error fetching HouseListing: {ex.Message}, {ex.InnerException}, {ex.StackTrace}");
        }

    }
    
    
    [HttpGet("OwnerId")]
    public async Task<IActionResult> GetListingsByOwner(int ownerId, [FromServices] IHouseProfileRepository profileRepo)
    {
        try
        {
            var response = _repo.GetAll();
            
            var toReturn = new List<HouseListingDto>();
            foreach (var listing in response)
            {
                listing.Profile = await profileRepo.GetSingleAsync(listing.ProfileId);
                toReturn.Add(listing);
            }
            
            return Ok(toReturn.AsQueryable().Where(l => l.Profile.OwnerId == ownerId));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
            Console.WriteLine(ex.InnerException);
            Console.WriteLine(ex.StackTrace);
            return StatusCode(500, $"Error fetching HouseListing: {ex.Message}\n{ex.InnerException}\n{ex.StackTrace}");
        }
    }
    
    // GET api/HouseListing/GetListingsByOwnerStatus/{ownerId}/{status}?includeApplications=true&includeProfiles=true
    
    [HttpGet("{ownerId:int}/{status}")]
    public async Task<IActionResult> GetListingsByOwnerStatus(
        [FromServices] IHouseProfileRepository profileRepo,
        [FromServices] IApplicationRepository applicationRepo,
        [FromServices] IHouseSitterRepository sitterRepo,
        [FromQuery] bool includeApplications,
        [FromQuery] bool includeProfiles,
        int ownerId, string status)
    {
        try
        {
            var response = _repo.GetAll();
            
            var toReturn = new List<HouseListingDto>();

            foreach (var listing in response)
            {
                if (includeProfiles)
                { 
                    listing.Profile = await profileRepo.GetSingleAsync(listing.ProfileId);
                }
                
                if (includeApplications)
                { 
                    listing.Applications = applicationRepo.GetAll()
                        .Where(a => a.ListingId == listing.Id)
                        .Select(a => new ApplicationDto{Status = a.Status, 
                            Sitter = new HouseSitterDto{Name = sitterRepo.GetSingleAsync(a.SitterId).Result.Name}})
                        .ToList();
                }

                toReturn.Add(listing);
            }

            return Ok(ownerId == 0 ? response.Where(l => l.Status == status) : toReturn.AsQueryable().Where(l => l.Profile.OwnerId == ownerId && l.Status == status));
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
            Console.WriteLine(ex.InnerException);
            Console.WriteLine(ex.StackTrace);
            return StatusCode(500, $"Error fetching HouseListing: {ex.Message}\n{ex.InnerException}\n{ex.StackTrace}");
        }
    }
    
    [HttpGet("{sitterId}")]
    public async Task<IActionResult> GetWaitingForReviewBySitter(
        [FromServices] IHouseReviewRepository reviewRepo,
        [FromServices] IApplicationRepository applicationRepo,
        [FromServices] IHouseProfileRepository profileRepo,
        [FromQuery] bool includeProfiles,
        int sitterId)
    {
        try
        {
            var confirmedApps = applicationRepo.GetAll()
                .Where(a => a.SitterId == sitterId && a.Status == "Confirmed")
                .Select(a => a.ListingId)
                .ToList();

            var reviews = reviewRepo.GetAll()
                .Where(r => r.SitterId == sitterId)
                .Select(r => r.ProfileId)
                .ToList();

            var response = _repo.GetAll().Where(l => confirmedApps.Contains(l.Id) && !reviews.Contains(l.ProfileId) && l.EndDate < DateOnly.FromDateTime(DateTime.Now));

            if (!includeProfiles) return Ok(response.ToList());
            
            foreach (var listing in response)
            {
                var profile = await profileRepo.GetSingleAsync(listing.ProfileId);
                listing.Profile = new HouseProfileDto
                {
                    Title = profile.Title,
                    Pictures = profile.Pictures,
                };
            }

            return Ok(response.ToList());
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Error fetching HouseListing: {ex.Message}\n{ex.InnerException}\n{ex.StackTrace}");
        }
    }
    
    // POST: api/HouseListing
    [HttpPost]
    public async Task<IActionResult> CreateHouseListing(
        [FromBody] CreateHouseListingDto createDto)
    {
        try
        {
            var response = await _repo.AddAsync(createDto);
            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500,
                $"Error creating HouseListing: {ex.Message} \n{ex.InnerException} \n{ex.StackTrace}");
        }
    }

    // PUT: api/HouseListing
    [HttpPut]
    public async Task<IActionResult> UpdateHouseListing(
        [FromBody] UpdateHouseListingDto updateDto)
    {
        try
        {
            var response = await _repo.UpdateAsync(updateDto);
            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500,
                $"Error updating HouseListing: {ex.Message}, {ex.InnerException}, {ex.StackTrace}");
        }
    }

    // DELETE: api/HouseListing/{id}
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteHouseListing(int id)
    {
        try
        {
            await _repo.DeleteAsync(id);
            return Ok();
        }
        catch (Exception ex)
        {
            return StatusCode(500,
                $"Error deleting HouseListing: {ex.Message}");
        }
    }

    [HttpGet] 
    public async Task<IActionResult> GetFilteredListings(
        [FromServices] IHouseProfileRepository profileRepo,
        [FromQuery] FilteredHouseListingsDto filter)
    {
        try
        {
            var listings = _repo.GetAll().Where(l => l.Status == "Open");
            foreach (var listing in listings)
            {
                listing.Profile = await profileRepo.GetSingleAsync(listing.ProfileId);
            }

            if(!string.IsNullOrWhiteSpace(filter.Region))
                listings = listings
                    .Where(l => l.Profile.Region == filter.Region);
                    
            if(!string.IsNullOrWhiteSpace(filter.City))
                listings = listings
                    .Where(l => l.Profile.City == filter.City);

            if (filter.StartDay.HasValue)
                listings = listings
                    .Where(l => Math.Abs(
                        l.StartDate.ToDateTime(new TimeOnly()).Subtract(new DateTime(filter.StartYear.Value, filter.StartMonth.Value, filter.StartDay.Value)).TotalDays) < 6);
            
            if (filter.EndDay.HasValue)
                listings = listings
                    .Where(l => Math.Abs(
                        l.EndDate.ToDateTime(new TimeOnly()).Subtract(new DateTime(filter.EndYear.Value, filter.EndMonth.Value, filter.EndDay.Value)).TotalDays) < 6);
                
            if (filter.Amenities.Any())
                listings = listings
                    .Where(l => l.Profile.Amenities.Intersect(filter.Amenities).Any());
            
            if (filter.Chores.Any())
                listings = listings
                    .Where(l => l.Profile.Chores.Intersect(filter.Chores).Any());
            
            return Ok(listings.ToList());
        }
        catch (Exception ex)
        {
            return StatusCode(500,
                $"Error getting filtered HouseListings: {ex.Message}");
        }
    }
}