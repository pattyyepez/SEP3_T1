﻿
using DTOs.Login;
using Microsoft.JSInterop;

namespace HousePalClient.Auth;

using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;
using System.Text.Json;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;

public class SimpleAuthProvider : AuthenticationStateProvider
{
    private readonly HttpClient _httpClient;
    private readonly IJSRuntime _jsRuntime;

    public SimpleAuthProvider(HttpClient httpClient, IJSRuntime jsRuntime)
    {
        _httpClient = httpClient;
        _jsRuntime = jsRuntime;
    }
    
    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        string userAsJson = "";
        try
        {
            userAsJson = await _jsRuntime.InvokeAsync<string>("sessionStorage.getItem", "currentUser");
        }
        catch (InvalidOperationException e)
        {
            return new AuthenticationState(new());
        }
    
        if (string.IsNullOrEmpty(userAsJson))
        {
            return new AuthenticationState(new());
        }
    
        UserDto userDto = JsonSerializer.Deserialize<UserDto>(userAsJson)!;
        List<Claim> claims = new List<Claim>
        {
            new Claim(ClaimTypes.Name, userDto.Name),
            new Claim(ClaimTypes.Email, userDto.Email),
            new Claim("Id", userDto.UserId.ToString()),
            new Claim(ClaimTypes.MobilePhone, userDto.Phone),
            new Claim("ProfilePicture", userDto.ProfilePicture),
            new Claim("CPR", userDto.CPR),
            new Claim("IsVerified", userDto.IsVerified.ToString()),
            new Claim("Biography", userDto.Biography)
        };
    
        if (userDto.Address != null)
        {
            claims.Add(new Claim("Address", userDto.Address));
            claims.Add(new Claim(ClaimTypes.Role, "HouseOwner"));
        }
    
        else
        {
            claims.Add(new Claim("Pictures", userDto.Pictures.ToString()));
            claims.Add(new Claim("Skills", userDto.Skills.ToString()));
            claims.Add(new Claim(ClaimTypes.Role, "HouseSitter"));
        }
        ClaimsIdentity identity = new ClaimsIdentity(claims, "apiauth");
        ClaimsPrincipal claimsPrincipal = new ClaimsPrincipal(identity);
        return new AuthenticationState(claimsPrincipal);
    
    }
    
    public async Task Login(string email, string password)
    {
        var response = await _httpClient.PostAsJsonAsync("auth/login",
            new LoginRequest
            {
                Email = email, 
                Password = password
            });
        
        if (!response.IsSuccessStatusCode)
        {
            throw new Exception("Invalid login attempt");
        }
    
        var userDto = await response.Content.ReadFromJsonAsync<UserDto>(new JsonSerializerOptions()
        {
            PropertyNameCaseInsensitive = true
        });
        
        string serialisedData = JsonSerializer.Serialize(userDto);
        await _jsRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentUser", serialisedData);
    
        List<Claim> claims = new List<Claim>
        {
            new Claim(ClaimTypes.Email, userDto.Email),
            new Claim("Id", userDto.UserId.ToString()),
            new Claim(ClaimTypes.MobilePhone, userDto.Phone),
            new Claim("ProfilePicture", userDto.ProfilePicture),
            new Claim("CPR", userDto.CPR),
            new Claim("IsVerified", userDto.IsVerified.ToString()),
            new Claim("Biography", userDto.Biography)
        };
    
        if (userDto.Address != null)
        {
            claims.Add(new Claim("Address", userDto.Address));
            claims.Add(new Claim(ClaimTypes.Role, "HouseOwner"));
        }
    
        else
        {
            claims.Add(new Claim("Pictures", userDto.Pictures.ToString()));
            claims.Add(new Claim("Skills", userDto.Skills.ToString()));
            claims.Add(new Claim(ClaimTypes.Role, "HouseSitter"));
        }
        
        var identity = new ClaimsIdentity(claims, "apiauth");
        ClaimsPrincipal claimsPrincipal = new ClaimsPrincipal(identity);
    
        NotifyAuthenticationStateChanged(
            Task.FromResult(
                new AuthenticationState(claimsPrincipal)
                )
            );
    }

    public async void Logout()
    {
        await _jsRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentUser", "");
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(new())));
    }
    public bool HasAccessToResource(int resourceId)
    {
        Console.WriteLine("Checking access for resource ID: " + resourceId);
        var authState = GetAuthenticationStateAsync().Result;
        var user = authState.User;

        if (!user.Identity.IsAuthenticated)
        {
            Console.WriteLine("User is NOT authenticated.");
            return false;
        }

        var userIdClaim = user.FindFirst("Id")?.Value;
        Console.WriteLine("Authenticated user ID: " + userIdClaim);
        return userIdClaim != null && userIdClaim == resourceId.ToString();
    }

    
        // private readonly HttpClient _httpClient;
    // private ClaimsPrincipal _currentClaimsPrincipal;
    // public SimpleAuthProvider(HttpClient httpClient)
    // {
    //     _httpClient = httpClient;
    // }
    // public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    // {
    //     return new AuthenticationState(_currentClaimsPrincipal ?? new ());
    // }
        // public async Task Login(string email, string password)
    // {
    //     HttpResponseMessage response = await _httpClient.PostAsJsonAsync(
    //         "auth/login",
    //         new LoginRequest
    //         {
    //             Email = email,
    //             Password = password
    //         });
    //
    //     string content = await response.Content.ReadAsStringAsync();
    //     if (!response.IsSuccessStatusCode)
    //     {
    //         throw new Exception(content);
    //     }
    //     UserDto userDto = JsonSerializer.Deserialize<UserDto>(content, new JsonSerializerOptions
    //     {
    //         PropertyNameCaseInsensitive = true
    //     })!;
    //
    //
    //     List<Claim> claims = new List<Claim>
    //     {
    //         new Claim(ClaimTypes.Email, userDto.Email),
    //         new Claim(ClaimTypes.Email, userDto.Email),
    //         new Claim("Id", userDto.UserId.ToString()),
    //         new Claim(ClaimTypes.MobilePhone, userDto.Phone),
    //         new Claim("ProfilePicture", userDto.ProfilePicture),
    //         new Claim("CPR", userDto.CPR),
    //         new Claim("IsVerified", userDto.IsVerified.ToString()),
    //         new Claim("Biography", userDto.Biography)
    //     };
    //
    //     if (userDto.Address != null)
    //     {
    //         claims.Add(new Claim("Address", userDto.Address));
    //         claims.Add(new Claim(ClaimTypes.Role, "HouseOwner"));
    //     }
    //
    //     else
    //     {
    //         claims.Add(new Claim("Pictures", userDto.Pictures.ToString()));
    //         claims.Add(new Claim("Skills", userDto.Skills.ToString()));
    //         claims.Add(new Claim(ClaimTypes.Role, "HouseSitter"));
    //     }
    //     
    //     
    //     ClaimsIdentity identity = new ClaimsIdentity(claims, "apiauth");
    //     _currentClaimsPrincipal = new ClaimsPrincipal(identity);
    //     
    //     NotifyAuthenticationStateChanged(
    //         Task.FromResult(new AuthenticationState(_currentClaimsPrincipal))
    //     );
    //
    // }
    // public void Logout()
    // {
    //     _currentClaimsPrincipal = new();
    //     NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(_currentClaimsPrincipal)));
    // }

}
