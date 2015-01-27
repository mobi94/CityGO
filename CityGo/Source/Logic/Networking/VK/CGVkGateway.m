//
//  CGVkGateway.m
//  CityGo
//
//  Created by ruslan on 1/26/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGVkGateway.h"

@implementation CGVkGateway

- (void)loginWithSuccess:(VKLoginSuccessBlock)successBlock failure:(VKLoginFailureBlock)failureBlock
{
    static NSString *const ALL_USER_FIELDS = @"id, first_name, last_name, sex, photo_400_orig, bdate";
    
    __block VKRequest *request = [[VKRequest alloc] init];
    
    request = [[VKApi users] get:@{ VK_API_FIELDS : ALL_USER_FIELDS }];
    
    request.debugTiming = YES;
    request.requestTimeout = 10;
    [request executeWithResultBlock:^(VKResponse *response)
    {
        NSData *jsonData = [response.responseString dataUsingEncoding:NSUTF8StringEncoding];
        NSError *error;
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
        if (!error)
        {
            successBlock(dict[@"response"][0]);
        }
        
        request = nil;
    }
    errorBlock: ^(NSError *error)
    {
        failureBlock(error);
        request = nil;
    }];
}

@end
