//
//  CGTwitterGateway.m
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGTwitterGateway.h"
#import "FHSTwitterEngine.h"

@interface CGTwitterGateway ()

@end

@implementation CGTwitterGateway

- (void)login:(UIViewController *)controller WithSuccess:(TwitterLoginSuccessBlock)successBlock failure:(TwitterLoginFailureBlock)failureBlock
{
    [[FHSTwitterEngine sharedEngine] permanentlySetConsumerKey:@"nHPo913oTTzoLeylNt7DT6ImH" andSecret:@"ZIhZm8Cmv7W0wREoiwBaDSrCMoSxbaHhl7OoKctyuoU12jwUKx"];
    [[FHSTwitterEngine sharedEngine] loadAccessToken];
    
    if (![[FHSTwitterEngine sharedEngine] isAuthorized])
    {
        UIViewController *loginController = [[FHSTwitterEngine sharedEngine] loginControllerWithCompletionHandler:^(BOOL success)
                                             {
                                                 NSLog(success?@"L0L success":@"O noes!!! Loggen faylur!!!");
                                                 if (success)
                                                 {
                                                     NSDictionary *twitterInfo = (NSDictionary *)[[FHSTwitterEngine sharedEngine] verifyCredentials];
                                                     
                                                     successBlock(twitterInfo);
                                                 }
                                                 else
                                                 {
                                                     NSError *error = [NSError errorWithDomain:@"Twitter signIn error" code:666 userInfo:nil];
                                                     failureBlock(error);
                                                 }
                                             }];
        [controller presentViewController:loginController animated:YES completion:nil];
    }
    else
    {
        NSDictionary *twitterInfo = (NSDictionary *)[[FHSTwitterEngine sharedEngine] verifyCredentials];
        
        successBlock(twitterInfo);
    }
}

@end
