﻿using Microsoft.AspNetCore.Mvc;

namespace RESTAPI.ExceptionHandling;

public class GlobalExceptionHandlerMiddleware : IMiddleware
{
    private readonly ILogger<GlobalExceptionHandlerMiddleware> _logger;

    public GlobalExceptionHandlerMiddleware(ILogger<GlobalExceptionHandlerMiddleware> logger)
    {
        _logger = logger;
    }
    
    public async Task InvokeAsync(HttpContext context, RequestDelegate next)
    {
        try
        {
            await next(context);
        }
        catch (Exception ex)
        {
            {
                var traceId = Guid.NewGuid();
                _logger.LogError($"Error occurred while processing the request, TraceId : ${traceId}, Message : ${ex.Message}, StackTrace: ${ex.StackTrace}");

                Console.WriteLine(ex.Message);
                context.Response.StatusCode = StatusCodes.Status500InternalServerError;

                // var problemDetails = new ProblemDetails
                // {
                //     Type = "https://tools.ietf.org/html/rfc7231#section-6.6.1",
                //     Title = "Internal Server Error",
                //     Status = (int)StatusCodes.Status500InternalServerError,
                //     Instance = context.Request.Path,
                //     Detail = $"Internal server error occured, traceId : {traceId}, Message : ${ex.Message}",
                // };
                await context.Response.WriteAsJsonAsync(ex.Message);
            }
        }
    }
}