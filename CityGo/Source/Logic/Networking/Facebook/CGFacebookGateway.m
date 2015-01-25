//
//  CGFacebookGateway.m
//  CityGo
//
//  Created by ruslan on 1/25/15.
//  Copyright (c) 2015 Ruslan Moskalenko. All rights reserved.
//

#import "CGFacebookGateway.h"
#import <FacebookSDK/FacebookSDK.h>

@interface CGFacebookGateway ()

- (void)sessionStateChanged:(FBSession *)session
                      state:(FBSessionState)state
                      error:(NSError *)error;

@end

@implementation CGFacebookGateway

- (void)sessionStateChanged:(FBSession *)session state:(FBSessionState)state error:(NSError *)error
{
    NSLog(@"%@", [[[FBSession activeSession] accessTokenData] accessToken]);
}

- (void)loginWithSuccess:(FBLoginSuccessBlock)successBlock failure:(FBLoginFailureBlock)failureBlock
{
    [[FBSession activeSession] closeAndClearTokenInformation];
    
    FBSession *newSession = [[FBSession alloc] initWithPermissions:@[@"public_profile", @"email", @"user_friends"]];
    [FBSession setActiveSession:newSession];
    
    [newSession openWithCompletionHandler:^(FBSession *session, FBSessionState status, NSError *error)
     {
         if (status == FBSessionStateOpen)
         {
             [FBRequestConnection startForMeWithCompletionHandler:^(FBRequestConnection *connection, id<FBGraphUser> user, NSError *error)
             {
                  if (!error)
                  {
                      NSMutableDictionary *fbUser = [NSMutableDictionary dictionary];
                      
                      fbUser[@"username"] = [NSString stringWithFormat:@"%@%@", user.last_name, user.first_name];
                      fbUser[@"password"] = user.objectID;
                      fbUser[@"gender"] = [user objectForKey:@"gender"];
                      fbUser[@"email"] = [user objectForKey:@"email"];
                      fbUser[@"avatarLink"] = [NSString stringWithFormat:@"http://graph.facebook.com/%@/picture?type=large", user.objectID];
                      
                      successBlock(fbUser);
                  }
              }];
         }
         else if (status == FBSessionStateClosedLoginFailed)
         {
             NSLog(@"%@", session);
             
             [session closeAndClearTokenInformation];
             
             NSError *customError = [NSError errorWithDomain:@"FacebookErrorInterrupted" code:-999 userInfo:nil];
             
             failureBlock(customError);
         }
     }];
}

@end
